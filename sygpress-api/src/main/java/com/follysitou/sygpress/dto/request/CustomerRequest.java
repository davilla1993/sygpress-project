package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String phoneNumber;

    private String address;
}
