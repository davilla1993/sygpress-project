package com.follysitou.sygpress.service;

import com.follysitou.sygpress.dto.request.ChangePasswordRequest;
import com.follysitou.sygpress.dto.request.LoginRequest;
import com.follysitou.sygpress.dto.request.RegisterRequest;
import com.follysitou.sygpress.dto.response.AuthResponse;
import com.follysitou.sygpress.dto.response.UserResponse;
import com.follysitou.sygpress.enums.Role;
import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.User;
import com.follysitou.sygpress.repository.UserRepository;
import com.follysitou.sygpress.security.CustomUserDetails;
import com.follysitou.sygpress.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", request.getUsername()));

        if (!user.getEnabled()) {
            auditLogService.logFailure("LOGIN", "User", user.getPublicId(),
                    "Tentative de connexion sur compte désactivé", "Compte désactivé", httpRequest);
            throw new RuntimeException("Compte désactivé. Contactez l'administrateur.");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        auditLogService.logSuccess("LOGIN", "User", user.getPublicId(),
                "Connexion réussie", httpRequest);

        return AuthResponse.builder()
                .token(token)
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .mustChangePassword(user.getMustChangePassword())
                .build();
    }

    @Transactional
    public UserResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Utilisateur", "username", request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Utilisateur", "email", request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .mustChangePassword(true)
                .build();

        User saved = userRepository.save(user);

        auditLogService.logSuccess("CREATE_USER", "User", saved.getPublicId(),
                "Création utilisateur: " + saved.getUsername(), httpRequest);

        return toUserResponse(saved);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, HttpServletRequest httpRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", username));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            auditLogService.logFailure("CHANGE_PASSWORD", "User", user.getPublicId(),
                    "Ancien mot de passe incorrect", "Mot de passe incorrect", httpRequest);
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        auditLogService.logSuccess("CHANGE_PASSWORD", "User", user.getPublicId(),
                "Changement de mot de passe", httpRequest);
    }

    @Transactional
    public void disableUser(String publicId, HttpServletRequest httpRequest) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "publicId", publicId));

        user.setEnabled(false);
        userRepository.save(user);

        auditLogService.logSuccess("DISABLE_USER", "User", publicId,
                "Désactivation utilisateur: " + user.getUsername(), httpRequest);
    }

    @Transactional
    public void enableUser(String publicId, HttpServletRequest httpRequest) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "publicId", publicId));

        user.setEnabled(true);
        userRepository.save(user);

        auditLogService.logSuccess("ENABLE_USER", "User", publicId,
                "Activation utilisateur: " + user.getUsername(), httpRequest);
    }

    @Transactional
    public void deleteUser(String publicId, HttpServletRequest httpRequest) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "publicId", publicId));

        // Soft delete
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        userRepository.save(user);

        auditLogService.logSuccess("DELETE_USER", "User", publicId,
                "Suppression utilisateur: " + user.getUsername(), httpRequest);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByPublicId(String publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "publicId", publicId));
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findByDeletedFalse(pageable).map(this::toUserResponse);
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setPublicId(user.getPublicId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setEnabled(user.getEnabled());
        response.setMustChangePassword(user.getMustChangePassword());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
