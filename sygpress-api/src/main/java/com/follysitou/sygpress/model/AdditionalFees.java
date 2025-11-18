package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalFees extends BaseEntity {


    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne peut pas dépasser 100 caractères")
    private String title;                                   // Exemple : "Livraison normale", "Livraison express"

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    private String description;                             // Livraison dans 24H, livraison dans 72H

    @DecimalMin(value = "0.0", message = "Le montant ne peut pas être négatif")
    @NotNull(message = "Le montant est obligatoire")
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}
