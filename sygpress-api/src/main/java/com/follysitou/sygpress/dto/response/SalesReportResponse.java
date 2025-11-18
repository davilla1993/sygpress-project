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
public class SalesReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalInvoices;
    private BigDecimal totalRevenue;
    private BigDecimal totalPaid;
    private BigDecimal totalUnpaid;
    private BigDecimal totalDiscount;
    private BigDecimal averageInvoiceAmount;
    private List<DailySales> dailySales;
    private List<ServiceSales> serviceBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySales {
        private LocalDate date;
        private int invoiceCount;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceSales {
        private String serviceName;
        private int quantity;
        private BigDecimal amount;
    }
}
