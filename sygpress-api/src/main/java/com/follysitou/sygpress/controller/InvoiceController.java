package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.AdditionalFeesRequest;
import com.follysitou.sygpress.dto.request.InvoiceLineRequest;
import com.follysitou.sygpress.dto.request.InvoiceRequest;
import com.follysitou.sygpress.dto.response.InvoiceResponse;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.mapper.InvoiceMapper;
import com.follysitou.sygpress.model.*;
import com.follysitou.sygpress.repository.CustomerRepository;
import com.follysitou.sygpress.repository.PricingRepository;
import com.follysitou.sygpress.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceMapper invoiceMapper;
    private final CustomerRepository customerRepository;
    private final PricingRepository pricingRepository;

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setDepositDate(request.getDepositDate());
        invoice.setDeliveryDate(request.getDeliveryDate());
        invoice.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        invoice.setAmountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO);

        // Set customer
        Customer customer = customerRepository.findByPublicId(request.getCustomerPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "publicId", request.getCustomerPublicId()));
        invoice.setCustomer(customer);

        // Set invoice lines
        List<InvoiceLine> invoiceLines = new ArrayList<>();
        for (InvoiceLineRequest lineRequest : request.getInvoiceLines()) {
            InvoiceLine line = new InvoiceLine();
            line.setQuantity(lineRequest.getQuantity());

            Pricing pricing = pricingRepository.findByPublicId(lineRequest.getPricingPublicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tarif", "publicId", lineRequest.getPricingPublicId()));
            line.setPricing(pricing);

            invoiceLines.add(line);
        }
        invoice.setInvoiceLines(invoiceLines);

        // Set additional fees
        if (request.getAdditionalFees() != null) {
            List<AdditionalFees> additionalFees = new ArrayList<>();
            for (AdditionalFeesRequest feeRequest : request.getAdditionalFees()) {
                AdditionalFees fee = new AdditionalFees();
                fee.setTitle(feeRequest.getTitle());
                fee.setDescription(feeRequest.getDescription());
                fee.setAmount(feeRequest.getAmount());
                additionalFees.add(fee);
            }
            invoice.setAdditionalFees(additionalFees);
        }

        Invoice saved = invoiceService.createInvoice(invoice);
        return new ResponseEntity<>(invoiceMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<InvoiceResponse> getByPublicId(@PathVariable String publicId) {
        Invoice invoice = invoiceService.findByPublicId(publicId);
        return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<InvoiceResponse> getByInvoiceNumber(@PathVariable String invoiceNumber) {
        Invoice invoice = invoiceService.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Facture", "numéro", invoiceNumber));
        return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getAll(Pageable pageable) {
        Page<Invoice> invoices = invoiceService.findAll(pageable);
        return ResponseEntity.ok(invoices.map(invoiceMapper::toResponse));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> delete(@PathVariable String publicId) {
        invoiceService.deleteByPublicId(publicId);
        return ResponseEntity.noContent().build();
    }
}
