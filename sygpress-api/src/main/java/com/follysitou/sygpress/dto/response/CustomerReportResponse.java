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
public class CustomerReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalCustomers;
    private int newCustomers;
    private List<CustomerStats> topCustomers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerStats {
        private String customerName;
        private String customerPhone;
        private int invoiceCount;
        private BigDecimal totalSpent;
        private BigDecimal totalUnpaid;
    }
}
