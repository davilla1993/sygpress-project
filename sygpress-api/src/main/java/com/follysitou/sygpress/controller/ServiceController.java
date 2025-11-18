package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.ServiceRequest;
import com.follysitou.sygpress.dto.response.ServiceResponse;
import com.follysitou.sygpress.mapper.ServiceMapper;
import com.follysitou.sygpress.service.LaundryServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getById(@PathVariable Long id) {
        com.follysitou.sygpress.model.Service service = laundryServiceService.findById(id);
        return ResponseEntity.ok(serviceMapper.toResponse(service));
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAll() {
        List<com.follysitou.sygpress.model.Service> services = laundryServiceService.findAll();
        return ResponseEntity.ok(serviceMapper.toResponseList(services));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> update(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        com.follysitou.sygpress.model.Service serviceDetails = new com.follysitou.sygpress.model.Service();
        serviceDetails.setName(request.getName());

        com.follysitou.sygpress.model.Service updated = laundryServiceService.update(id, serviceDetails);
        return ResponseEntity.ok(serviceMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        laundryServiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
