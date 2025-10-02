package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;
}
