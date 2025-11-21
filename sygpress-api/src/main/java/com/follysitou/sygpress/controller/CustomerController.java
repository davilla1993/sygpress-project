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
import com.follysitou.sygpress.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final InvoiceService invoiceService;
    private final InvoiceMapper invoiceMapper;
    private final AuditLogService auditLogService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request, HttpServletRequest httpRequest) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());

        Customer saved = customerService.create(customer);

        auditLogService.logSuccess("CREATE_CUSTOMER", "Customer", saved.getPublicId(),
                "Création client: " + saved.getName() + " (" + saved.getPhoneNumber() + ")", httpRequest);

        return new ResponseEntity<>(customerMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<Customer> customers = customerService.findAllList();
        List<CustomerResponse> response = customers.stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CustomerResponse> getByPublicId(@PathVariable String publicId) {
        Customer customer = customerService.findByPublicId(publicId);
        return ResponseEntity.ok(customerMapper.toResponse(customer));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<CustomerResponse>> getAll(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<Customer> customers;
        if (search != null && !search.trim().isEmpty()) {
            customers = customerService.searchByName(search.trim(), pageable);
        } else {
            customers = customerService.findAll(pageable);
        }
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
    public ResponseEntity<CustomerResponse> update(@PathVariable String publicId,
                                                   @Valid @RequestBody CustomerRequest request,
                                                   HttpServletRequest httpRequest) {

        Customer customerDetails = new Customer();
        customerDetails.setName(request.getName());
        customerDetails.setPhoneNumber(request.getPhoneNumber());
        customerDetails.setAddress(request.getAddress());

        Customer updated = customerService.update(publicId, customerDetails);

        auditLogService.logSuccess("UPDATE_CUSTOMER", "Customer", publicId,
                "Modification client: " + updated.getName(), httpRequest);

        return ResponseEntity.ok(customerMapper.toResponse(updated));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String publicId, HttpServletRequest httpRequest) {
        Customer customer = customerService.findByPublicId(publicId);
        String customerName = customer.getName();

        customerService.delete(publicId);

        auditLogService.logSuccess("DELETE_CUSTOMER", "Customer", publicId,
                "Suppression client: " + customerName, httpRequest);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{publicId}/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<InvoiceResponse>> getCustomerInvoices(@PathVariable String publicId, Pageable pageable) {
        Page<Invoice> invoices = invoiceService.findByCustomerPublicId(publicId, pageable);
        return ResponseEntity.ok(invoices.map(invoiceMapper::toResponse));
    }
}
