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
public class UserReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalUsers;
    private BigDecimal totalRevenue;
    private List<UserStats> userStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private Long userId;
        private String userName;
        private String userEmail;
        private String userRole;
        private int invoiceCount;
        private BigDecimal totalRevenue;
        private BigDecimal totalPaid;
        private BigDecimal totalUnpaid;
        private BigDecimal averageInvoiceAmount;
        private double percentage;
    }
}
