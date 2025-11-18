package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceRequest {

    @NotNull(message = "La date de dépôt est obligatoire")
    private LocalDate depositDate;

    @NotNull(message = "La date de livraison est obligatoire")
    private LocalDate deliveryDate;

    @DecimalMin(value = "0.0", message = "La remise ne peut pas être négative")
    private BigDecimal discount;

    @DecimalMin(value = "0.0", message = "Le montant payé ne peut pas être négatif")
    private BigDecimal amountPaid;

    @NotNull(message = "Le client est obligatoire")
    private Long customerId;

    @NotEmpty(message = "Au moins une ligne de facture est requise")
    private List<InvoiceLineRequest> invoiceLines;

    private List<AdditionalFeesRequest> additionalFees;
}
