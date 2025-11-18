package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.ServiceRequest;
import com.follysitou.sygpress.dto.response.ServiceResponse;
import com.follysitou.sygpress.mapper.ServiceMapper;
import com.follysitou.sygpress.model.LaundryService;
import com.follysitou.sygpress.service.LaundryServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final LaundryServiceService laundryServiceService;
    private final ServiceMapper serviceMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        LaundryService service = new LaundryService();
        service.setName(request.getName());

        LaundryService saved = laundryServiceService.create(service);
        return new ResponseEntity<>(serviceMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> getByPublicId(@PathVariable String publicId) {
        LaundryService service = laundryServiceService.findByPublicId(publicId);
        return ResponseEntity.ok(serviceMapper.toResponse(service));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ServiceResponse>> getAll(Pageable pageable) {
        Page<LaundryService> services = laundryServiceService.findAll(pageable);
        return ResponseEntity.ok(services.map(serviceMapper::toResponse));
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> update(@PathVariable String publicId, @Valid @RequestBody ServiceRequest request) {
        LaundryService serviceDetails = new LaundryService();
        serviceDetails.setName(request.getName());

        LaundryService updated = laundryServiceService.update(publicId, serviceDetails);
        return ResponseEntity.ok(serviceMapper.toResponse(updated));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String publicId) {
        laundryServiceService.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}
