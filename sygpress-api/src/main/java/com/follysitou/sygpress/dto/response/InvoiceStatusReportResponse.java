package com.follysitou.sygpress.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceStatusReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalInvoices;
    private int paidInvoices;
    private int unpaidInvoices;
    private int partiallyPaidInvoices;
    private BigDecimal totalAmount;
    private BigDecimal totalPaid;
    private BigDecimal totalRemaining;
    private List<ProcessingStatusCount> processingStatusBreakdown;
    private List<UnpaidInvoice> unpaidInvoicesList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingStatusCount {
        private String status;
        private int count;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnpaidInvoice {
        private String invoiceNumber;
        private String customerName;
        private LocalDate depositDate;
        private BigDecimal totalAmount;
        private BigDecimal remainingAmount;
    }
}
