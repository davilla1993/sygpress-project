package com.follysitou.sygpress.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageDTO {

    private Long id;

    @NotBlank(message = "Le nom est requis")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    private String email;

    @Pattern(regexp = "^[\\d\\s\\+\\-\\(\\)]*$", message = "Le format du téléphone est invalide")
    private String phone;

    @NotBlank(message = "Le sujet est requis")
    @Size(min = 3, max = 200, message = "Le sujet doit contenir entre 3 et 200 caractères")
    private String subject;

    @NotBlank(message = "Le message est requis")
    @Size(min = 10, max = 2000, message = "Le message doit contenir entre 10 et 2000 caractères")
    private String message;

    private LocalDateTime createdAt;

    private Boolean readStatus;
}
