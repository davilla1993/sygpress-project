package com.follysitou.sygpress.service;

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
        // Charger en deux étapes pour éviter le Cartesian product
        Invoice invoice = invoiceRepository.findByPublicIdWithLines(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture", "publicId", publicId));

        // Charger les frais supplémentaires
        invoiceRepository.findByPublicIdWithFees(publicId);

        return invoice;
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> findAll(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> findByCustomerPublicId(String customerPublicId, Pageable pageable) {
        return invoiceRepository.findByCustomerPublicIdAndDeletedFalse(customerPublicId, pageable);
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
