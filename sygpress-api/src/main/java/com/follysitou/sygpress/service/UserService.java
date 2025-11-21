package com.follysitou.sygpress.service;

import com.follysitou.sygpress.dto.request.UserUpdateRequest;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.User;
import com.follysitou.sygpress.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
    private static final int PASSWORD_LENGTH = 10;

    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public User findByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "publicId", publicId));
    }

    @Transactional
    public User toggleStatus(String publicId) {
        User user = findByPublicId(publicId);
        user.setEnabled(!user.getEnabled());
        return userRepository.save(user);
    }

    @Transactional
    public User update(String publicId, UserUpdateRequest request) {
        User user = findByPublicId(publicId);

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        // Use firstName and lastName directly if provided
        if (request.getFirstName() != null || request.getLastName() != null) {
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
        }
        // Fallback: parse fullName to extract firstName and lastName (for backward compatibility)
        else if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            String[] nameParts = request.getFullName().trim().split("\\s+", 2);
            user.setFirstName(nameParts[0]);
            user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        }

        return userRepository.save(user);
    }

    @Transactional
    public void delete(String publicId) {
        User user = findByPublicId(publicId);
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Transactional
    public String resetPassword(String publicId) {
        User user = findByPublicId(publicId);

        // Générer un nouveau mot de passe temporaire
        String temporaryPassword = generateRandomPassword();

        // Encoder et définir le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(temporaryPassword));

        // Forcer le changement de mot de passe à la prochaine connexion
        user.setMustChangePassword(true);

        userRepository.save(user);

        return temporaryPassword;
    }

    private String generateRandomPassword() {
        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return password.toString();
    }
}
