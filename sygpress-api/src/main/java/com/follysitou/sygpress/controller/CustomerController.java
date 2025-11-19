package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.CustomerRequest;
import com.follysitou.sygpress.dto.response.CustomerResponse;
import com.follysitou.sygpress.dto.response.InvoiceResponse;
import com.follysitou.sygpress.mapper.CustomerMapper;
import com.follysitou.sygpress.mapper.InvoiceMapper;
import com.follysitou.sygpress.model.Customer;
import com.follysitou.sygpress.model.Invoice;
import com.follysitou.sygpress.service.CustomerService;
import com.follysitou.sygpress.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final InvoiceService invoiceService;
    private final InvoiceMapper invoiceMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());

        Customer saved = customerService.create(customer);
        return new ResponseEntity<>(customerMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CustomerResponse> getByPublicId(@PathVariable String publicId) {
        Customer customer = customerService.findByPublicId(publicId);
        return ResponseEntity.ok(customerMapper.toResponse(customer));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<CustomerResponse>> getAll(Pageable pageable) {
        Page<Customer> customers = customerService.findAll(pageable);
        return ResponseEntity.ok(customers.map(customerMapper::toResponse));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<CustomerResponse>> searchByName(@RequestParam String name, Pageable pageable) {
        Page<Customer> customers = customerService.searchByName(name, pageable);
        return ResponseEntity.ok(customers.map(customerMapper::toResponse));
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CustomerResponse> update(@PathVariable String publicId, @Valid @RequestBody CustomerRequest request) {
        Customer customerDetails = new Customer();
        customerDetails.setName(request.getName());
        customerDetails.setPhoneNumber(request.getPhoneNumber());
        customerDetails.setAddress(request.getAddress());

        Customer updated = customerService.update(publicId, customerDetails);
        return ResponseEntity.ok(customerMapper.toResponse(updated));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String publicId) {
        customerService.delete(publicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{publicId}/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<InvoiceResponse>> getCustomerInvoices(@PathVariable String publicId, Pageable pageable) {
        Page<Invoice> invoices = invoiceService.findByCustomerPublicId(publicId, pageable);
        return ResponseEntity.ok(invoices.map(invoiceMapper::toResponse));
    }
}
