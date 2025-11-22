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
public class ServiceReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalServices;
    private BigDecimal totalRevenue;
    private List<ServiceStats> serviceStats;
    private List<ArticleStats> articleStats;
    private List<CombinationStats> combinationStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceStats {
        private String serviceName;
        private int quantity;
        private BigDecimal amount;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleStats {
        private String articleName;
        private int quantity;
        private BigDecimal amount;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CombinationStats {
        private String serviceName;
        private String articleName;
        private int quantity;
        private BigDecimal amount;
    }
}
