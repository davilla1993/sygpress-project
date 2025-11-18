package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Min(value = 1, message = "La quantité doit être au moins de 1")
    private int quantity;

    @DecimalMin(value = "0.0", message = "Le montant ne peut pas être négatif")
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;      // calculé: quantity * pricing.price

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    @NotNull(message = "La facture est obligatoire")
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_id")
    @NotNull(message = "Le tarif est obligatoire")
    private Pricing pricing;     // lien direct vers l'article + service + prix

    // Calcule automatiquement le montant
    public void calculateAmount() {
        if (pricing != null && pricing.getPrice() != null) {
            this.amount = pricing.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}
