package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    private double amount; // calculé: quantity * pricing.price

    @ManyToOne
    private Invoice invoice;

    @ManyToOne
    private Pricing pricing; // lien direct vers l'article + service + prix
}
