package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.CompanyRequest;
import com.follysitou.sygpress.dto.response.CompanyResponse;
import com.follysitou.sygpress.mapper.CompanyMapper;
import com.follysitou.sygpress.model.Company;
import com.follysitou.sygpress.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Tag(name = "Company", description = "Gestion de la configuration entreprise")
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;

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
    public ResponseEntity<CompanyResponse> uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
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
    public ResponseEntity<byte[]> getLogo() {
        return companyService.getCompany()
                .filter(company -> company.getLogo() != null && company.getLogo().length > 0)
                .map(company -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(company.getLogoContentType() != null
                                ? company.getLogoContentType()
                                : "image/png"))
                        .body(company.getLogo()))
                .orElse(ResponseEntity.notFound().build());
    }
}
