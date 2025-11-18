package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.CompanyRequest;
import com.follysitou.sygpress.dto.response.CompanyResponse;
import com.follysitou.sygpress.mapper.CompanyMapper;
import com.follysitou.sygpress.model.Company;
import com.follysitou.sygpress.service.CompanyService;
import com.follysitou.sygpress.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Tag(name = "Company", description = "Gestion de la configuration entreprise")
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;
    private final FileStorageService fileStorageService;

    @GetMapping
    @Operation(summary = "Récupérer la configuration entreprise")
    public ResponseEntity<CompanyResponse> getCompany() {
        return companyService.getCompany()
                .map(company -> ResponseEntity.ok(companyMapper.toResponse(company)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer ou mettre à jour la configuration entreprise")
    public ResponseEntity<CompanyResponse> createOrUpdateCompany(@Valid @RequestBody CompanyRequest request) {
        Company company = companyService.createOrUpdateCompany(request);
        return ResponseEntity.status(HttpStatus.OK).body(companyMapper.toResponse(company));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Télécharger le logo de l'entreprise")
    public ResponseEntity<CompanyResponse> uploadLogo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }

        Company company = companyService.updateLogo(file);
        return ResponseEntity.ok(companyMapper.toResponse(company));
    }

    @DeleteMapping("/logo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer le logo de l'entreprise")
    public ResponseEntity<Void> deleteLogo() {
        companyService.deleteLogo();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logo")
    @Operation(summary = "Récupérer le logo de l'entreprise")
    public ResponseEntity<Resource> getLogo() {
        return companyService.getCompany()
                .filter(company -> company.getLogoPath() != null && !company.getLogoPath().isEmpty())
                .map(company -> {
                    Resource resource = fileStorageService.loadFileAsResource(company.getLogoPath());
                    String contentType = fileStorageService.getContentType(company.getLogoPath());

                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"logo\"")
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
