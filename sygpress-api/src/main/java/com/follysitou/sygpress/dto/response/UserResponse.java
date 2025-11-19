package com.follysitou.sygpress.dto.response;

import com.follysitou.sygpress.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String publicId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private Role role;
    private Boolean enabled;
    private Boolean active;
    private Boolean mustChangePassword;
    private LocalDateTime createdAt;
}
