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
        response.setId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setDepositDate(invoice.getDepositDate());
        response.setDeliveryDate(invoice.getDeliveryDate());
        response.setDiscount(invoice.getDiscount());
        response.setVatAmount(invoice.getVatAmount());
        response.setAmountPaid(invoice.getAmountPaid());
        response.setRemainingAmount(invoice.getRemainingAmount());
        response.setInvoicePaid(invoice.isInvoicePaid());

        // Calcul du montant total
        response.setTotalAmount(calculateTotalAmount(invoice));

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

        return response;
    }

    private InvoiceLineResponse invoiceLineToResponse(InvoiceLine invoiceLine) {
        InvoiceLineResponse response = new InvoiceLineResponse();
        response.setId(invoiceLine.getId());
        response.setQuantity(invoiceLine.getQuantity());
        response.setAmount(invoiceLine.getAmount());

        if (invoiceLine.getPricing() != null) {
            response.setPricing(pricingMapper.toResponse(invoiceLine.getPricing()));
        }

        return response;
    }

    private AdditionalFeesResponse additionalFeesToResponse(AdditionalFees fees) {
        AdditionalFeesResponse response = new AdditionalFeesResponse();
        response.setId(fees.getId());
        response.setTitle(fees.getTitle());
        response.setDescription(fees.getDescription());
        response.setAmount(fees.getAmount());
        return response;
    }

    private BigDecimal calculateTotalAmount(Invoice invoice) {
        BigDecimal totalLines = invoice.getInvoiceLines().stream()
                .map(InvoiceLine::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = invoice.getAdditionalFees().stream()
                .map(AdditionalFees::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = invoice.getDiscount() != null ? invoice.getDiscount() : BigDecimal.ZERO;

        return totalLines.add(totalFees).subtract(discount);
    }
}
