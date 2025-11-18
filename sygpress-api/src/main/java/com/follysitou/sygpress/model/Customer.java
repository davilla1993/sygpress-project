package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {


    @Column(unique = true)
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^[0-9\\+\\-\\(\\)\\s]{8,20}$", message = "Format de numéro de téléphone invalide")
    private String phoneNumber;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    private String address;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Invoice> invoices = new ArrayList<>();
}
