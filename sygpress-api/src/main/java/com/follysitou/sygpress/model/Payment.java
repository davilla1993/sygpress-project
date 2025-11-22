package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant un paiement effectué sur une facture.
 * Permet de tracer l'historique complet des encaissements.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class Payment extends BaseEntity {

    @NotNull(message = "Le montant du paiement est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    @Column(precision = 10, scale = 0, nullable = false)
    private BigDecimal amount;

    @NotNull(message = "La facture est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private String paidBy;  // Email/nom de la personne qui a effectué le paiement

    private String paymentMethod;  // Mode de paiement (espèces, carte, virement, etc.)
    private String notes;  // Notes optionnelles sur le paiement

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }
}
