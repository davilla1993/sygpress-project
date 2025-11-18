package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.CustomerRequest;
import com.follysitou.sygpress.dto.response.CustomerResponse;
import com.follysitou.sygpress.mapper.CustomerMapper;
import com.follysitou.sygpress.model.Customer;
import com.follysitou.sygpress.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());

        Customer saved = customerService.create(customer);
        return new ResponseEntity<>(customerMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        Customer customer = customerService.findById(id);
        return ResponseEntity.ok(customerMapper.toResponse(customer));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        List<Customer> customers = customerService.findAll();
        return ResponseEntity.ok(customerMapper.toResponseList(customers));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchByName(@RequestParam String name) {
        List<Customer> customers = customerService.searchByName(name);
        return ResponseEntity.ok(customerMapper.toResponseList(customers));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        Customer customerDetails = new Customer();
        customerDetails.setName(request.getName());
        customerDetails.setPhoneNumber(request.getPhoneNumber());
        customerDetails.setAddress(request.getAddress());

        Customer updated = customerService.update(id, customerDetails);
        return ResponseEntity.ok(customerMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
