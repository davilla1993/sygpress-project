package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalFees {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    private String title;                                   // Exemple : "Livraison normale", "Livraison express"

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    private String description;                             // Livraison dans 24H, livraison dans 72H


    @Min(value = 0, message = "Le montant ne peut pas être négatif")
    @NotNull(message = "Le montant est obligatoire")
    private double amount;

    @ManyToOne
    private Invoice invoice;
}
