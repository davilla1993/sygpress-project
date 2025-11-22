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
    private BigDecimal vatRate;  // Taux de TVA en % (ex: 18.00 pour 18%)
    private BigDecimal amountPaid;
    private BigDecimal remainingAmount;
    private boolean invoicePaid;
    private ProcessingStatus processingStatus;
    private CustomerResponse customer;
    private List<InvoiceLineResponse> invoiceLines;
    private List<AdditionalFeesResponse> additionalFees;
    private List<PaymentResponse> payments;  // Historique complet des paiements

    // Champs calculés
    private BigDecimal subtotalAmount; // Montant HT (Hors Taxes)
    private BigDecimal vatAmount;      // Montant de TVA calculé
    private BigDecimal totalAmount;    // Montant TTC (Toutes Taxes Comprises)

    private String createdBy;
    private LocalDateTime createdAt;

    // Traçabilité des paiements
    private String lastPaymentBy;  // Email/nom de la personne qui a effectué le dernier paiement
    private LocalDateTime lastPaymentAt;  // Date et heure du dernier paiement
}
