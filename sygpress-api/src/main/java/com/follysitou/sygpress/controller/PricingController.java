package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.PricingRequest;
import com.follysitou.sygpress.dto.response.PricingResponse;
import com.follysitou.sygpress.mapper.PricingMapper;
import com.follysitou.sygpress.model.Pricing;
import com.follysitou.sygpress.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;
    private final PricingMapper pricingMapper;

    @PostMapping
    public ResponseEntity<PricingResponse> create(@Valid @RequestBody PricingRequest request) {
        Pricing saved = pricingService.create(request.getPrice(), request.getArticleId(), request.getServiceId());
        return new ResponseEntity<>(pricingMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PricingResponse> getById(@PathVariable Long id) {
        Pricing pricing = pricingService.findById(id);
        return ResponseEntity.ok(pricingMapper.toResponse(pricing));
    }

    @GetMapping
    public ResponseEntity<List<PricingResponse>> getAll() {
        List<Pricing> pricings = pricingService.findAll();
        return ResponseEntity.ok(pricingMapper.toResponseList(pricings));
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<PricingResponse>> getByArticle(@PathVariable Long articleId) {
        List<Pricing> pricings = pricingService.findByArticle(articleId);
        return ResponseEntity.ok(pricingMapper.toResponseList(pricings));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<PricingResponse>> getByService(@PathVariable Long serviceId) {
        List<Pricing> pricings = pricingService.findByService(serviceId);
        return ResponseEntity.ok(pricingMapper.toResponseList(pricings));
    }

    @GetMapping("/article/{articleId}/service/{serviceId}")
    public ResponseEntity<PricingResponse> getByArticleAndService(
            @PathVariable Long articleId, @PathVariable Long serviceId) {
        Pricing pricing = pricingService.findByArticleAndService(articleId, serviceId);
        return ResponseEntity.ok(pricingMapper.toResponse(pricing));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PricingResponse> update(@PathVariable Long id, @Valid @RequestBody PricingRequest request) {
        Pricing updated = pricingService.update(id, request.getPrice(), request.getArticleId(), request.getServiceId());
        return ResponseEntity.ok(pricingMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pricingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
