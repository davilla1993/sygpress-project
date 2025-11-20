package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.UserUpdateRequest;
import com.follysitou.sygpress.dto.response.PasswordResetResponse;
import com.follysitou.sygpress.dto.response.UserResponse;
import com.follysitou.sygpress.mapper.UserMapper;
import com.follysitou.sygpress.model.User;
import com.follysitou.sygpress.service.UserService;
import com.follysitou.sygpress.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAll(Pageable pageable) {
        Page<User> users = userService.findAll(pageable);
        return ResponseEntity.ok(users.map(userMapper::toResponse));
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getByPublicId(@PathVariable String publicId) {
        User user = userService.findByPublicId(publicId);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(
            @PathVariable String publicId,
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        User user = userService.update(publicId, request);

        auditLogService.logSuccess("UPDATE_USER", "User", publicId,
                "Modification utilisateur: " + user.getUsername(), httpRequest);

        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PatchMapping("/{publicId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable String publicId, HttpServletRequest httpRequest) {
        User user = userService.toggleStatus(publicId);

        String action = user.getEnabled() ? "ENABLE_USER" : "DISABLE_USER";
        String details = (user.getEnabled() ? "Activation" : "Désactivation") + " utilisateur: " + user.getUsername();

        auditLogService.logSuccess(action, "User", publicId, details, httpRequest);

        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PostMapping("/{publicId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PasswordResetResponse> resetPassword(@PathVariable String publicId, HttpServletRequest httpRequest) {
        User user = userService.findByPublicId(publicId);
        String temporaryPassword = userService.resetPassword(publicId);

        auditLogService.logSuccess("RESET_PASSWORD", "User", publicId,
                "Réinitialisation mot de passe par ADMIN pour: " + user.getUsername(), httpRequest);

        PasswordResetResponse response = new PasswordResetResponse(
                "Mot de passe réinitialisé avec succès",
                temporaryPassword,
                user.getUsername()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String publicId, HttpServletRequest httpRequest) {
        User user = userService.findByPublicId(publicId);
        String username = user.getUsername();

        userService.delete(publicId);

        auditLogService.logSuccess("DELETE_USER", "User", publicId,
                "Suppression utilisateur: " + username, httpRequest);

        return ResponseEntity.noContent().build();
    }
}
