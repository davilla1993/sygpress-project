package com.follysitou.sygpress.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company")
public class Company extends BaseEntity {

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    @Column(length = 255)
    private String address;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    @Column(length = 100)
    private String city;


    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    @Column(length = 100)
    private String country;

    @Size(max = 20, message = "Le numéro de téléphone ne peut pas dépasser 20 caractères")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    @Column(length = 100)
    private String email;

    @Size(max = 255, message = "Le site web ne peut pas dépasser 255 caractères")
    @Column(length = 255)
    private String website;

    @Lob
    @Column(name = "logo", columnDefinition = "BYTEA")
    private byte[] logo;

    @Size(max = 500, message = "Le slogan ne peut pas dépasser 500 caractères")
    @Column(length = 500)
    private String slogan;

    @Column(name = "vat_rate")
    private Double vatRate;
}
