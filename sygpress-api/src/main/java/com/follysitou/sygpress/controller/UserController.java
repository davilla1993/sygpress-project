package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.response.UserResponse;
import com.follysitou.sygpress.mapper.UserMapper;
import com.follysitou.sygpress.model.User;
import com.follysitou.sygpress.service.UserService;
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

    @PatchMapping("/{publicId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable String publicId) {
        User user = userService.toggleStatus(publicId);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String publicId) {
        userService.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}
