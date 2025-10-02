package com.follysitou.sygpress.dto.response;

import lombok.Data;

@Data
public class InvoiceLineResponse {

    private Long id;
    private int quantity;
    private double amount;
    private PricingResponse pricing;
}
