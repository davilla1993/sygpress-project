package com.follysitou.sygpress.dto.response;

import com.follysitou.sygpress.enums.ProcessingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceResponse {

    private String publicId;
    private String invoiceNumber;
    private LocalDate depositDate;
    private LocalDate deliveryDate;
    private BigDecimal discount;
    private BigDecimal vatAmount;
    private BigDecimal amountPaid;
    private BigDecimal remainingAmount;
    private boolean invoicePaid;
    private ProcessingStatus processingStatus;
    private CustomerResponse customer;
    private List<InvoiceLineResponse> invoiceLines;
    private List<AdditionalFeesResponse> additionalFees;
    private BigDecimal totalAmount; // Champ calculé
    private String createdBy;
    private LocalDateTime createdAt;
}
