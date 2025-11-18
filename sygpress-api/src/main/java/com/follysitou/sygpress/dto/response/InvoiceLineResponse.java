package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceLineResponse {

    private String publicId;
    private int quantity;
    private BigDecimal amount;
    private PricingResponse pricing;
}
