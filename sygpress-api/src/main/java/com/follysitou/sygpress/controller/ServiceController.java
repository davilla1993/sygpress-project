package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.ServiceRequest;
import com.follysitou.sygpress.dto.response.ServiceResponse;
import com.follysitou.sygpress.mapper.ServiceMapper;
import com.follysitou.sygpress.service.LaundryServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final LaundryServiceService laundryServiceService;
    private final ServiceMapper serviceMapper;

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        com.follysitou.sygpress.model.Service service = new com.follysitou.sygpress.model.Service();
        service.setName(request.getName());

        com.follysitou.sygpress.model.Service saved = laundryServiceService.create(service);
        return new ResponseEntity<>(serviceMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ServiceResponse> getByPublicId(@PathVariable String publicId) {
        com.follysitou.sygpress.model.Service service = laundryServiceService.findByPublicId(publicId);
        return ResponseEntity.ok(serviceMapper.toResponse(service));
    }

    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getAll(Pageable pageable) {
        Page<com.follysitou.sygpress.model.Service> services = laundryServiceService.findAll(pageable);
        return ResponseEntity.ok(services.map(serviceMapper::toResponse));
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<ServiceResponse> update(@PathVariable String publicId, @Valid @RequestBody ServiceRequest request) {
        com.follysitou.sygpress.model.Service serviceDetails = new com.follysitou.sygpress.model.Service();
        serviceDetails.setName(request.getName());

        com.follysitou.sygpress.model.Service updated = laundryServiceService.update(publicId, serviceDetails);
        return ResponseEntity.ok(serviceMapper.toResponse(updated));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> delete(@PathVariable String publicId) {
        laundryServiceService.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}
