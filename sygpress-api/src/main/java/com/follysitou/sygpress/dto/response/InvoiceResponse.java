package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private LocalDate depositDate;
    private LocalDate deliveryDate;
    private double discount;
    private double vatAmount;
    private double amountPaid;
    private double remainingAmount;
    private boolean invoicePaid;
    private CustomerResponse customer;
    private List<InvoiceLineResponse> invoiceLines;
    private List<AdditionalFeesResponse> additionalFees;
    private double totalAmount; // Champ calculé
}
