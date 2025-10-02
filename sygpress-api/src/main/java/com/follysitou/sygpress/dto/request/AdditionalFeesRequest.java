package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdditionalFeesRequest {

    @NotBlank
    private String title;

    private String description;

    @Min(0)
    private double amount;
}
