package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLine {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Min(value = 1, message = "La quantité doit être au moins de 1")
    private int quantity;

    @Min(value = 0, message = "Le montant ne peut pas être négatif")
    private double amount;      // calculé: quantity * pricing.price

    @ManyToOne
    private Invoice invoice;

    @ManyToOne
    private Pricing pricing;     // lien direct vers l'article + service + prix
}
