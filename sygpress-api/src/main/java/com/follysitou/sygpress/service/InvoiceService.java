package com.follysitou.sygpress.service;

import com.follysitou.sygpress.model.Invoice;
import com.follysitou.sygpress.model.InvoiceLine;
import com.follysitou.sygpress.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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
    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
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
    public void deleteById(Long id) {
        invoiceRepository.deleteById(id);
    }
}
