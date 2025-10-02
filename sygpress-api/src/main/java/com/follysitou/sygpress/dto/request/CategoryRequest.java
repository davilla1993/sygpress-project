package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;
}
