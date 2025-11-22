package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private String publicId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String paidBy;
    private String paymentMethod;
    private String notes;
}
