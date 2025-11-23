package com.follysitou.sygpress.service;

import com.follysitou.sygpress.enums.ProcessingStatus;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.Invoice;
import com.follysitou.sygpress.model.InvoiceLine;
import com.follysitou.sygpress.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceNumberGeneratorService numberGeneratorService;

    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        // Générer le numéro de facture dans le service (pas dans l'entité)
        if (invoice.getInvoiceNumber() == null) {
            invoice.setInvoiceNumber(numberGeneratorService.getNextInvoiceNumber());
        }

        // Calculer les montants des lignes
        if (invoice.getInvoiceLines() != null) {
            for (InvoiceLine line : invoice.getInvoiceLines()) {
                line.setInvoice(invoice);
                line.calculateAmount();
            }
        }

        // Associer les frais supplémentaires à la facture
        if (invoice.getAdditionalFees() != null) {
            invoice.getAdditionalFees().forEach(fee -> fee.setInvoice(invoice));
        }

        // Calculer le montant restant
        BigDecimal totalAmount = invoice.calculateTotalAmount();
        BigDecimal amountPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        invoice.setRemainingAmount(totalAmount.subtract(amountPaid));
        invoice.setInvoicePaid(invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0);

        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public Invoice findByPublicId(String publicId) {
        return invoiceRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture", "publicId", publicId));
    }

    @Transactional(readOnly = true)
    public Invoice findByPublicIdWithDetails(String publicId) {
        return invoiceRepository.findByPublicIdWithDetails(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture", "publicId", publicId));
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> findAll(Pageable pageable) {
        return invoiceRepository.findAllNonDeleted(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> findByCustomerPublicId(String customerPublicId, Pageable pageable) {
        return invoiceRepository.findByCustomerPublicIdAndDeletedFalse(customerPublicId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> searchInvoices(String search, ProcessingStatus status, Pageable pageable) {
        // Si on a à la fois une recherche et un statut
        if (search != null && !search.isEmpty() && status != null) {
            return invoiceRepository.searchInvoicesByStatus(search, status, pageable);
        }
        // Si on a seulement une recherche
        else if (search != null && !search.isEmpty()) {
            return invoiceRepository.searchInvoices(search, pageable);
        }
        // Si on a seulement un statut
        else if (status != null) {
            return invoiceRepository.findByProcessingStatus(status, pageable);
        }
        // Sinon, tout retourner
        else {
            return invoiceRepository.findAllNonDeleted(pageable);
        }
    }

    @Transactional
    public Invoice updateInvoice(Invoice invoice) {
        // Recalculer les montants
        if (invoice.getInvoiceLines() != null) {
            invoice.getInvoiceLines().forEach(InvoiceLine::calculateAmount);
        }

        BigDecimal totalAmount = invoice.calculateTotalAmount();
        BigDecimal amountPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        invoice.setRemainingAmount(totalAmount.subtract(amountPaid));
        invoice.setInvoicePaid(invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0);

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void deleteByPublicId(String publicId) {
        Invoice invoice = findByPublicId(publicId);
        invoiceRepository.delete(invoice);
    }
}
