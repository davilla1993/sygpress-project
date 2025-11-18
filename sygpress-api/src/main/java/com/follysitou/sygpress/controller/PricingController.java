package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.PricingRequest;
import com.follysitou.sygpress.dto.response.PricingResponse;
import com.follysitou.sygpress.mapper.PricingMapper;
import com.follysitou.sygpress.model.Pricing;
import com.follysitou.sygpress.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;
    private final PricingMapper pricingMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PricingResponse> create(@Valid @RequestBody PricingRequest request) {
        Pricing saved = pricingService.create(request.getPrice(), request.getArticlePublicId(), request.getServicePublicId());
        return new ResponseEntity<>(pricingMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PricingResponse> getByPublicId(@PathVariable String publicId) {
        Pricing pricing = pricingService.findByPublicId(publicId);
        return ResponseEntity.ok(pricingMapper.toResponse(pricing));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<PricingResponse>> getAll(Pageable pageable) {
        Page<Pricing> pricings = pricingService.findAll(pageable);
        return ResponseEntity.ok(pricings.map(pricingMapper::toResponse));
    }

    @GetMapping("/article/{articlePublicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<PricingResponse>> getByArticle(@PathVariable String articlePublicId, Pageable pageable) {
        Page<Pricing> pricings = pricingService.findByArticle(articlePublicId, pageable);
        return ResponseEntity.ok(pricings.map(pricingMapper::toResponse));
    }

    @GetMapping("/service/{servicePublicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<PricingResponse>> getByService(@PathVariable String servicePublicId, Pageable pageable) {
        Page<Pricing> pricings = pricingService.findByService(servicePublicId, pageable);
        return ResponseEntity.ok(pricings.map(pricingMapper::toResponse));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/article/{articlePublicId}/service/{servicePublicId}")
    public ResponseEntity<PricingResponse> getByArticleAndService(
            @PathVariable String articlePublicId, @PathVariable String servicePublicId) {
        Pricing pricing = pricingService.findByArticleAndService(articlePublicId, servicePublicId);
        return ResponseEntity.ok(pricingMapper.toResponse(pricing));
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PricingResponse> update(@PathVariable String publicId, @Valid @RequestBody PricingRequest request) {
        Pricing updated = pricingService.update(publicId, request.getPrice(), request.getArticlePublicId(), request.getServicePublicId());
        return ResponseEntity.ok(pricingMapper.toResponse(updated));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String publicId) {
        pricingService.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}
