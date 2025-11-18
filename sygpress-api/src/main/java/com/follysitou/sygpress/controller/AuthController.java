package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.ChangePasswordRequest;
import com.follysitou.sygpress.dto.request.LoginRequest;
import com.follysitou.sygpress.dto.request.RegisterRequest;
import com.follysitou.sygpress.dto.request.ResetPasswordRequest;
import com.follysitou.sygpress.dto.response.AuthResponse;
import com.follysitou.sygpress.dto.response.UserResponse;
import com.follysitou.sygpress.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  HttpServletRequest httpRequest) {
        return new ResponseEntity<>(authService.register(request, httpRequest), HttpStatus.CREATED);
    }

    @PostMapping("/auth/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                HttpServletRequest httpRequest) {
        authService.changePassword(request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(authService.getAllUsers(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{publicId}")
    public ResponseEntity<UserResponse> getUserByPublicId(@PathVariable String publicId) {
        return ResponseEntity.ok(authService.getUserByPublicId(publicId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{publicId}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable String publicId, HttpServletRequest httpRequest) {
        authService.disableUser(publicId, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{publicId}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable String publicId, HttpServletRequest httpRequest) {
        authService.enableUser(publicId, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{publicId}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable String publicId,
                                               @Valid @RequestBody ResetPasswordRequest request,
                                               HttpServletRequest httpRequest) {
        authService.resetPassword(publicId, request.getNewPassword(), httpRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/users/{publicId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String publicId, HttpServletRequest httpRequest) {
        authService.deleteUser(publicId, httpRequest);
        return ResponseEntity.noContent().build();
    }
}
