package com.follysitou.sygpress.dto.response;

import com.follysitou.sygpress.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String publicId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean mustChangePassword;
}
