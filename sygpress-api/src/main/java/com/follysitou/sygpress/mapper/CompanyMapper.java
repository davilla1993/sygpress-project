package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.response.CompanyResponse;
import com.follysitou.sygpress.model.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public CompanyResponse toResponse(Company company) {
        if (company == null) {
            return null;
        }

        String logoUrl = null;
        if (company.getLogoPath() != null && !company.getLogoPath().isEmpty()) {
            logoUrl = "/api/company/logo";
        }

        return CompanyResponse.builder()
                .publicId(company.getPublicId())
                .name(company.getName())
                .address(company.getAddress())
                .city(company.getCity())
                .country(company.getCountry())
                .phoneNumber(company.getPhoneNumber())
                .email(company.getEmail())
                .website(company.getWebsite())
                .logoUrl(logoUrl)
                .slogan(company.getSlogan())
                .vatRate(company.getVatRate())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
