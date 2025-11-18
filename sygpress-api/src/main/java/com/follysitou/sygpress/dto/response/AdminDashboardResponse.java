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
public class AdminDashboardResponse {

    // Statistiques globales
    private int totalCustomers;
    private int totalInvoices;
    private int totalUsers;
    private BigDecimal totalRevenue;

    // Statistiques du jour
    private int todayInvoices;
    private BigDecimal todayRevenue;
    private BigDecimal todayPayments;
    private int todayNewCustomers;

    // Statistiques du mois
    private int monthInvoices;
    private BigDecimal monthRevenue;
    private BigDecimal monthPayments;
    private int monthNewCustomers;

    // Statistiques de paiement
    private BigDecimal totalPaid;
    private BigDecimal totalUnpaid;
    private double paymentRate;

    // Répartition par statut de traitement
    private List<ProcessingStatusStat> processingStatusStats;

    // Évolution des ventes (7 derniers jours)
    private List<DailyStat> last7DaysSales;

    // Évolution des ventes (12 derniers mois)
    private List<MonthlyStat> last12MonthsSales;

    // Top 5 clients
    private List<TopCustomer> topCustomers;

    // Top 5 services
    private List<TopService> topServices;

    // Factures récentes
    private List<RecentInvoice> recentInvoices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingStatusStat {
        private String status;
        private int count;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStat {
        private LocalDate date;
        private int invoiceCount;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStat {
        private String month;
        private int year;
        private int invoiceCount;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private String name;
        private String phone;
        private int invoiceCount;
        private BigDecimal totalSpent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopService {
        private String serviceName;
        private int quantity;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentInvoice {
        private String invoiceNumber;
        private String customerName;
        private LocalDate depositDate;
        private BigDecimal amount;
        private String status;
        private boolean paid;
    }
}
