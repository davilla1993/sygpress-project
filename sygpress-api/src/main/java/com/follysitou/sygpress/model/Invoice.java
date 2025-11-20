package com.follysitou.sygpress.model;

import com.follysitou.sygpress.enums.ProcessingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String invoiceNumber;

    @NotNull(message = "La date de dépôt est obligatoire")
    private LocalDate depositDate;

    @NotNull(message = "La date de livraison est obligatoire")
    @FutureOrPresent(message = "La date de livraison doit être aujourd'hui ou dans le futur")
    private LocalDate deliveryDate;

    @DecimalMin(value = "0.0", message = "La remise ne peut pas être négative")
    @Column(precision = 10, scale = 0)
    private BigDecimal discount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Le taux de TVA ne peut pas être négatif")
    @Column(precision = 5, scale = 2)
    private BigDecimal vatRate = BigDecimal.ZERO;  // Taux de TVA en % (ex: 18.00 pour 18%)

    @DecimalMin(value = "0.0", message = "Le montant payé ne peut pas être négatif")
    @Column(precision = 10, scale = 0)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Le montant restant ne peut pas être négatif")
    @Column(precision = 10, scale = 0)
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    private boolean invoicePaid;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProcessingStatus processingStatus = ProcessingStatus.DEPOT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLine> invoiceLines = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdditionalFees> additionalFees = new ArrayList<>();

    // Méthode utilitaire pour calculer le montant HT (Hors Taxes)
    public BigDecimal calculateSubtotalAmount() {
        BigDecimal totalLines = invoiceLines.stream()
                .map(InvoiceLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = additionalFees.stream()
                .map(AdditionalFees::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalLines.add(totalFees).subtract(discount != null ? discount : BigDecimal.ZERO);
    }

    // Méthode pour calculer le montant de TVA en FCFA (arrondi à l'entier)
    public BigDecimal calculateVatAmount() {
        if (vatRate == null || vatRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = calculateSubtotalAmount();
        // Calcul: montantHT * (taux / 100), arrondi à l'entier supérieur
        return subtotal.multiply(vatRate)
                .divide(new BigDecimal("100"), 0, java.math.RoundingMode.HALF_UP);
    }

    // Méthode pour calculer le montant total TTC (Toutes Taxes Comprises)
    public BigDecimal calculateTotalAmount() {
        return calculateSubtotalAmount().add(calculateVatAmount());
    }
}
