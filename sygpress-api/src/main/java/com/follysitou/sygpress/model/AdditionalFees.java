package com.follysitou.sygpress.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalFees {

    @Id
    @GeneratedValue
    private Long id;
    private String title;    // Exemple : "Livraison normale", "Livraison express"
    private String description;  // Livraison dans 24H, livraison dans 72H
    private double amount;

    @ManyToOne
    private Invoice invoice;
}
