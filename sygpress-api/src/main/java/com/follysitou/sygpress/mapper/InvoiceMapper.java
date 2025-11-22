package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.response.*;
import com.follysitou.sygpress.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvoiceMapper {

    private final CustomerMapper customerMapper;
    private final PricingMapper pricingMapper;

    public InvoiceMapper(CustomerMapper customerMapper, PricingMapper pricingMapper) {
        this.customerMapper = customerMapper;
        this.pricingMapper = pricingMapper;
    }

    public List<InvoiceResponse> toResponseList(List<Invoice> invoices) {
        return invoices.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setPublicId(invoice.getPublicId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setDepositDate(invoice.getDepositDate());
        response.setDeliveryDate(invoice.getDeliveryDate());
        response.setDiscount(invoice.getDiscount());
        response.setVatRate(invoice.getVatRate());
        response.setAmountPaid(invoice.getAmountPaid());
        response.setRemainingAmount(invoice.getRemainingAmount());
        response.setInvoicePaid(invoice.isInvoicePaid());
        response.setProcessingStatus(invoice.getProcessingStatus());
        response.setCreatedBy(invoice.getCreatedBy());
        response.setCreatedAt(invoice.getCreatedAt());
        response.setLastPaymentBy(invoice.getLastPaymentBy());
        response.setLastPaymentAt(invoice.getLastPaymentAt());

        // Calculs des montants
        response.setSubtotalAmount(invoice.calculateSubtotalAmount());  // Montant HT
        response.setVatAmount(invoice.calculateVatAmount());            // Montant TVA
        response.setTotalAmount(invoice.calculateTotalAmount());        // Montant TTC

        if (invoice.getCustomer() != null) {
            response.setCustomer(customerMapper.toResponse(invoice.getCustomer()));
        }

        if (invoice.getInvoiceLines() != null) {
            response.setInvoiceLines(invoice.getInvoiceLines().stream()
                    .map(this::invoiceLineToResponse)
                    .collect(Collectors.toList()));
        }

        if (invoice.getAdditionalFees() != null) {
            response.setAdditionalFees(invoice.getAdditionalFees().stream()
                    .map(this::additionalFeesToResponse)
                    .collect(Collectors.toList()));
        }

        if (invoice.getPayments() != null) {
            response.setPayments(invoice.getPayments().stream()
                    .map(this::paymentToResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private InvoiceLineResponse invoiceLineToResponse(InvoiceLine invoiceLine) {
        InvoiceLineResponse response = new InvoiceLineResponse();
        response.setPublicId(invoiceLine.getPublicId());
        response.setQuantity(invoiceLine.getQuantity());
        response.setAmount(invoiceLine.getAmount());

        if (invoiceLine.getPricing() != null) {
            response.setPricing(pricingMapper.toResponse(invoiceLine.getPricing()));
        }

        return response;
    }

    private AdditionalFeesResponse additionalFeesToResponse(AdditionalFees fees) {
        AdditionalFeesResponse response = new AdditionalFeesResponse();
        response.setPublicId(fees.getPublicId());
        response.setTitle(fees.getTitle());
        response.setDescription(fees.getDescription());
        response.setAmount(fees.getAmount());
        return response;
    }

    private PaymentResponse paymentToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPublicId(payment.getPublicId());
        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaidBy(payment.getPaidBy());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setNotes(payment.getNotes());
        return response;
    }
}
