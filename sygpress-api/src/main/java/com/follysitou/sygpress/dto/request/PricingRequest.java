package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PricingRequest {

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix ne peut pas être négatif")
    private BigDecimal price;

    @NotBlank(message = "L'article est obligatoire")
    private String articlePublicId;

    @NotBlank(message = "Le service est obligatoire")
    private String servicePublicId;
}
