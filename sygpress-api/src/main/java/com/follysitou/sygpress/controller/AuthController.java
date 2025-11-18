package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.ChangePasswordRequest;
import com.follysitou.sygpress.dto.request.LoginRequest;
import com.follysitou.sygpress.dto.request.RegisterRequest;
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

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(authService.getAllUsers(pageable));
    }

    @GetMapping("/admin/users/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByPublicId(@PathVariable String publicId) {
        return ResponseEntity.ok(authService.getUserByPublicId(publicId));
    }

    @PutMapping("/admin/users/{publicId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disableUser(@PathVariable String publicId, HttpServletRequest httpRequest) {
        authService.disableUser(publicId, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/users/{publicId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enableUser(@PathVariable String publicId, HttpServletRequest httpRequest) {
        authService.enableUser(publicId, httpRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/users/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String publicId, HttpServletRequest httpRequest) {
        authService.deleteUser(publicId, httpRequest);
        return ResponseEntity.noContent().build();
    }
}
