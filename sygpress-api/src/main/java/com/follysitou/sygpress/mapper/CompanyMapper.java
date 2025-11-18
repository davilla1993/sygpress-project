package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.response.CompanyResponse;
import com.follysitou.sygpress.model.Company;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class CompanyMapper {

    public CompanyResponse toResponse(Company company) {
        if (company == null) {
            return null;
        }

        String logoBase64 = null;
        if (company.getLogo() != null && company.getLogo().length > 0) {
            logoBase64 = Base64.getEncoder().encodeToString(company.getLogo());
        }

        return CompanyResponse.builder()
                .publicId(company.getPublicId())
                .name(company.getName())
                .address(company.getAddress())
                .city(company.getCity())
                .postalCode(company.getPostalCode())
                .country(company.getCountry())
                .phoneNumber(company.getPhoneNumber())
                .email(company.getEmail())
                .website(company.getWebsite())
                .rccmNumber(company.getRccmNumber())
                .nifNumber(company.getNifNumber())
                .iban(company.getIban())
                .bic(company.getBic())
                .bankName(company.getBankName())
                .logoBase64(logoBase64)
                .logoContentType(company.getLogoContentType())
                .slogan(company.getSlogan())
                .legalMentions(company.getLegalMentions())
                .paymentTerms(company.getPaymentTerms())
                .vatRate(company.getVatRate())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
