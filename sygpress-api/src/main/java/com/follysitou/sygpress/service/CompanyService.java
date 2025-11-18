package com.follysitou.sygpress.service;

import com.follysitou.sygpress.dto.request.CompanyRequest;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.Company;
import com.follysitou.sygpress.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public Optional<Company> getCompany() {
        return companyRepository.findFirstByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public Company getCompanyOrThrow() {
        return companyRepository.findFirstByDeletedFalse()
                .orElseThrow(() -> new ResourceNotFoundException("Configuration entreprise non trouvée"));
    }

    @Transactional(readOnly = true)
    public Company findByPublicId(String publicId) {
        return companyRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration entreprise non trouvée avec l'ID: " + publicId));
    }

    @Transactional
    public Company createOrUpdateCompany(CompanyRequest request) {
        Company company = companyRepository.findFirstByDeletedFalse()
                .orElse(new Company());

        updateCompanyFromRequest(company, request);
        return companyRepository.save(company);
    }

    @Transactional
    public Company updateLogo(MultipartFile file) {
        Company company = getCompanyOrThrow();

        // Supprimer l'ancien logo s'il existe
        if (company.getLogoPath() != null && !company.getLogoPath().isEmpty()) {
            fileStorageService.deleteFile(company.getLogoPath());
        }

        // Stocker le nouveau fichier
        String logoPath = fileStorageService.storeFile(file, "logos");
        company.setLogoPath(logoPath);

        return companyRepository.save(company);
    }

    @Transactional
    public void deleteLogo() {
        Company company = getCompanyOrThrow();

        if (company.getLogoPath() != null && !company.getLogoPath().isEmpty()) {
            fileStorageService.deleteFile(company.getLogoPath());
            company.setLogoPath(null);
            companyRepository.save(company);
        }
    }

    private void updateCompanyFromRequest(Company company, CompanyRequest request) {
        company.setName(request.getName());
        company.setAddress(request.getAddress());
        company.setCity(request.getCity());
        company.setCountry(request.getCountry());
        company.setPhoneNumber(request.getPhoneNumber());
        company.setEmail(request.getEmail());
        company.setWebsite(request.getWebsite());
        company.setSlogan(request.getSlogan());
        company.setVatRate(request.getVatRate());
    }
}
