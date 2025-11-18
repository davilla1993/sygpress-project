package com.follysitou.sygpress.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    private String address;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String city;

    @Size(max = 20, message = "Le code postal ne peut pas dépasser 20 caractères")
    private String postalCode;

    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String country;

    @Size(max = 20, message = "Le numéro de téléphone ne peut pas dépasser 20 caractères")
    private String phoneNumber;

    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    @Size(max = 255, message = "Le site web ne peut pas dépasser 255 caractères")
    private String website;

    @Size(max = 50, message = "Le numéro RCCM ne peut pas dépasser 50 caractères")
    private String rccmNumber;

    @Size(max = 50, message = "Le NIF ne peut pas dépasser 50 caractères")
    private String nifNumber;

    @Size(max = 100, message = "L'IBAN ne peut pas dépasser 100 caractères")
    private String iban;

    @Size(max = 50, message = "Le BIC ne peut pas dépasser 50 caractères")
    private String bic;

    @Size(max = 100, message = "Le nom de la banque ne peut pas dépasser 100 caractères")
    private String bankName;

    @Size(max = 500, message = "Le slogan ne peut pas dépasser 500 caractères")
    private String slogan;

    @Size(max = 1000, message = "Les mentions légales ne peuvent pas dépasser 1000 caractères")
    private String legalMentions;

    @Size(max = 500, message = "Les conditions de paiement ne peuvent pas dépasser 500 caractères")
    private String paymentTerms;

    private Double vatRate;
}
