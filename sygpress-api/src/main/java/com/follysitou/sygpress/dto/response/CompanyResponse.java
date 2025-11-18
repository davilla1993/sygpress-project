package com.follysitou.sygpress.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private String publicId;
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String phoneNumber;
    private String email;
    private String website;
    private String rccmNumber;
    private String nifNumber;
    private String iban;
    private String bic;
    private String bankName;
    private String logoBase64;
    private String logoContentType;
    private String slogan;
    private String legalMentions;
    private String paymentTerms;
    private Double vatRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
