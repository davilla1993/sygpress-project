package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.AdditionalFeesRequest;
import com.follysitou.sygpress.dto.request.InvoiceLineRequest;
import com.follysitou.sygpress.dto.request.InvoiceRequest;
import com.follysitou.sygpress.dto.request.PaymentRequest;
import com.follysitou.sygpress.dto.response.InvoiceResponse;
import com.follysitou.sygpress.enums.ProcessingStatus;
import com.follysitou.sygpress.exception.BadRequestException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.mapper.InvoiceMapper;
import com.follysitou.sygpress.model.*;
import java.util.Map;
import com.follysitou.sygpress.repository.CustomerRepository;
import com.follysitou.sygpress.repository.PricingRepository;
import com.follysitou.sygpress.repository.PaymentRepository;
import com.follysitou.sygpress.service.InvoicePdfService;
import com.follysitou.sygpress.service.InvoiceService;
import com.follysitou.sygpress.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Gestion des factures")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceMapper invoiceMapper;
    private final CustomerRepository customerRepository;
    private final PricingRepository pricingRepository;
    private final PaymentRepository paymentRepository;
    private final InvoicePdfService invoicePdfService;
    private final AuditLogService auditLogService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest request, HttpServletRequest httpRequest) {
        Invoice invoice = new Invoice();
        invoice.setDepositDate(request.getDepositDate());
        invoice.setDeliveryDate(request.getDeliveryDate());
        invoice.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        invoice.setVatRate(request.getVatRate() != null ? request.getVatRate() : BigDecimal.ZERO);
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

        auditLogService.logSuccess("CREATE_INVOICE", "Invoice", saved.getPublicId(),
                "Création facture: " + saved.getInvoiceNumber() + " pour client: " + customer.getName(), httpRequest);

        return new ResponseEntity<>(invoiceMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Modifier une facture")
    public ResponseEntity<InvoiceResponse> update(
            @PathVariable String publicId,
            @Valid @RequestBody InvoiceRequest request,
            HttpServletRequest httpRequest) {

        Invoice invoice = invoiceService.findByPublicIdWithDetails(publicId);

        // Vérifier si la facture peut être modifiée
        validateInvoiceCanBeModified(invoice);

        // Update basic fields
        invoice.setDepositDate(request.getDepositDate());
        invoice.setDeliveryDate(request.getDeliveryDate());
        invoice.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        invoice.setVatRate(request.getVatRate() != null ? request.getVatRate() : BigDecimal.ZERO);
        invoice.setAmountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO);

        // Update customer
        Customer customer = customerRepository.findByPublicId(request.getCustomerPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "publicId", request.getCustomerPublicId()));
        invoice.setCustomer(customer);

        // Clear and update invoice lines
        invoice.getInvoiceLines().clear();
        List<InvoiceLine> invoiceLines = new ArrayList<>();
        for (InvoiceLineRequest lineRequest : request.getInvoiceLines()) {
            InvoiceLine line = new InvoiceLine();
            line.setQuantity(lineRequest.getQuantity());
            line.setInvoice(invoice);

            Pricing pricing = pricingRepository.findByPublicId(lineRequest.getPricingPublicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tarif", "publicId", lineRequest.getPricingPublicId()));
            line.setPricing(pricing);

            invoiceLines.add(line);
        }
        invoice.setInvoiceLines(invoiceLines);

        // Clear and update additional fees
        invoice.getAdditionalFees().clear();
        if (request.getAdditionalFees() != null) {
            List<AdditionalFees> additionalFees = new ArrayList<>();
            for (AdditionalFeesRequest feeRequest : request.getAdditionalFees()) {
                AdditionalFees fee = new AdditionalFees();
                fee.setTitle(feeRequest.getTitle());
                fee.setDescription(feeRequest.getDescription());
                fee.setAmount(feeRequest.getAmount());
                fee.setInvoice(invoice);
                additionalFees.add(fee);
            }
            invoice.setAdditionalFees(additionalFees);
        }

        Invoice updated = invoiceService.updateInvoice(invoice);

        auditLogService.logSuccess("UPDATE_INVOICE", "Invoice", publicId,
                "Modification facture: " + updated.getInvoiceNumber(), httpRequest);

        return ResponseEntity.ok(invoiceMapper.toResponse(updated));
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InvoiceResponse> getByPublicId(@PathVariable String publicId) {
        Invoice invoice = invoiceService.findByPublicId(publicId);
        return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @GetMapping("/number/{invoiceNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<InvoiceResponse> getByInvoiceNumber(@PathVariable String invoiceNumber) {
        Invoice invoice = invoiceService.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Facture", "numéro", invoiceNumber));
        return ResponseEntity.ok(invoiceMapper.toResponse(invoice));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Lister toutes les factures avec recherche et filtres")
    public ResponseEntity<Page<InvoiceResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProcessingStatus status,
            Pageable pageable) {
        Page<Invoice> invoices = invoiceService.searchInvoices(search, status, pageable);
        return ResponseEntity.ok(invoices.map(invoiceMapper::toResponse));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String publicId, HttpServletRequest httpRequest) {
        Invoice invoice = invoiceService.findByPublicId(publicId);
        String invoiceNumber = invoice.getInvoiceNumber();

        invoiceService.deleteByPublicId(publicId);

        auditLogService.logSuccess("DELETE_INVOICE", "Invoice", publicId,
                "Suppression facture: " + invoiceNumber, httpRequest);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{publicId}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Télécharger la facture en PDF")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String publicId) throws IOException {
        Invoice invoice = invoiceService.findByPublicId(publicId);
        byte[] pdfContent = invoicePdfService.generateInvoicePdf(publicId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "facture-" + invoice.getInvoiceNumber() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    @GetMapping("/{publicId}/print")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Imprimer la facture en PDF")
    public ResponseEntity<byte[]> printPdf(@PathVariable String publicId, HttpServletRequest httpRequest) throws IOException {
        Invoice invoice = invoiceService.findByPublicId(publicId);
        byte[] pdfContent = invoicePdfService.generateInvoicePdf(publicId);

        auditLogService.logSuccess("PRINT_INVOICE", "Invoice", publicId,
                "Impression facture: " + invoice.getInvoiceNumber(), httpRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "facture-" + invoice.getInvoiceNumber() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    @PatchMapping("/{publicId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Changer le statut d'une facture")
    public ResponseEntity<InvoiceResponse> updateStatus(
            @PathVariable String publicId,
            @RequestBody Map<String, String> statusUpdate,
            HttpServletRequest httpRequest) {
        Invoice invoice = invoiceService.findByPublicId(publicId);

        String statusStr = statusUpdate.get("status");
        if (statusStr == null) {
            throw new IllegalArgumentException("Le statut est requis");
        }

        ProcessingStatus oldStatus = invoice.getProcessingStatus();
        ProcessingStatus newStatus = ProcessingStatus.valueOf(statusStr);
        invoice.setProcessingStatus(newStatus);

        Invoice updated = invoiceService.updateInvoice(invoice);

        auditLogService.logSuccess("CHANGE_STATUS", "Invoice", publicId,
                "Changement statut facture " + updated.getInvoiceNumber() + ": " + oldStatus + " -> " + newStatus, httpRequest);

        return ResponseEntity.ok(invoiceMapper.toResponse(updated));
    }

    @PostMapping("/{publicId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Ajouter un paiement à une facture")
    public ResponseEntity<InvoiceResponse> addPayment(
            @PathVariable String publicId,
            @Valid @RequestBody PaymentRequest paymentRequest,
            HttpServletRequest httpRequest) {
        Invoice invoice = invoiceService.findByPublicId(publicId);

        // Récupérer l'utilisateur connecté pour la traçabilité
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication != null ? authentication.getName() : "SYSTEM";

        // Créer une entrée dans l'historique des paiements
        Payment payment = new Payment();
        payment.setAmount(paymentRequest.getAmount());
        payment.setInvoice(invoice);
        payment.setPaidBy(currentUser);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod("Espèces"); // Par défaut, peut être modifié ultérieurement
        invoice.getPayments().add(payment);

        // Ajouter le nouveau paiement au montant déjà payé
        BigDecimal currentAmountPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal newAmountPaid = currentAmountPaid.add(paymentRequest.getAmount());
        invoice.setAmountPaid(newAmountPaid);

        // Enregistrer qui a effectué ce paiement et quand (pour rétrocompatibilité)
        invoice.setLastPaymentBy(currentUser);
        invoice.setLastPaymentAt(LocalDateTime.now());

        // Mettre à jour la facture (le service recalculera automatiquement remainingAmount et invoicePaid)
        Invoice updated = invoiceService.updateInvoice(invoice);

        auditLogService.logSuccess("ADD_PAYMENT", "Invoice", publicId,
                "Ajout paiement de " + paymentRequest.getAmount() + " sur facture " + invoice.getInvoiceNumber() +
                " (Total payé: " + newAmountPaid + ") par " + currentUser, httpRequest);

        return ResponseEntity.ok(invoiceMapper.toResponse(updated));
    }

    /**
     * Vérifie si une facture peut être modifiée selon les règles de gestion
     * Une facture ne peut plus être modifiée si elle est LIVRE ou COLLECTE et totalement payée
     */
    private void validateInvoiceCanBeModified(Invoice invoice) {
        boolean isFullyPaid = invoice.isInvoicePaid() ||
                              (invoice.getRemainingAmount() != null &&
                               invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0);

        boolean isDeliveredOrCollected = invoice.getProcessingStatus() == ProcessingStatus.LIVRE ||
                                         invoice.getProcessingStatus() == ProcessingStatus.COLLECTE;

        if (isDeliveredOrCollected && isFullyPaid) {
            throw new BadRequestException(
                "Cette facture ne peut plus être modifiée car elle est " +
                invoice.getProcessingStatus() + " et totalement payée"
            );
        }
    }
}
