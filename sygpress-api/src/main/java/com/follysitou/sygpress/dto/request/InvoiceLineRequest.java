package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoiceLineRequest {

    @Min(1)
    private int quantity;

    @NotNull(message = "Le pricing est obligatoire")
    private Long pricingId;
}
