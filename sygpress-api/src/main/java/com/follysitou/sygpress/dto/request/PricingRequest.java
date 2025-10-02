package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PricingRequest {

    @NotNull(message = "Le prix est obligatoire")
    @Min(0)
    private Double price;

    @NotNull(message = "L'article est obligatoire")
    private Long articleId;

    @NotNull(message = "Le service est obligatoire")
    private Long serviceId;
}
