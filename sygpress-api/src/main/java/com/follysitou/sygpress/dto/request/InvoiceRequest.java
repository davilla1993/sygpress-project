package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceRequest {

    @NotNull(message = "La date de dépôt est obligatoire")
    private LocalDate depositDate;

    @NotNull(message = "La date de livraison est obligatoire")
    private LocalDate deliveryDate;

    @Min(0)
    private double discount;

    @Min(0)
    private double amountPaid;

    private boolean invoicePaid;

    @NotNull(message = "Le client est obligatoire")
    private Long customerId;

    @NotEmpty(message = "Au moins une ligne de facture est requise")
    private List<InvoiceLineRequest> invoiceLines;

    private List<AdditionalFeesRequest> additionalFees;
}
