package com.follysitou.sygpress.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetResponse {
    private String message;
    private String temporaryPassword;
    private String username;
}
