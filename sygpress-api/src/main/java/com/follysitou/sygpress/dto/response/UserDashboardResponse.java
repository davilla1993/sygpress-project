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
public class UserDashboardResponse {

    // Statistiques du jour
    private int todayInvoices;
    private BigDecimal todayRevenue;
    private BigDecimal todayPayments;
    private int todayDeliveries;

    // En-cours par statut de traitement
    private List<ProcessingQueue> processingQueues;

    // Factures à livrer aujourd'hui
    private List<DeliveryToday> deliveriesToday;

    // Factures en attente de paiement
    private List<PendingPayment> pendingPayments;

    // Alertes et notifications
    private List<Alert> alerts;

    // Dernières factures créées
    private List<RecentInvoice> recentInvoices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingQueue {
        private String status;
        private String statusLabel;
        private int count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryToday {
        private String invoiceNumber;
        private String customerName;
        private String customerPhone;
        private BigDecimal amount;
        private BigDecimal remainingAmount;
        private String processingStatus;
        private boolean ready;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingPayment {
        private String invoiceNumber;
        private String customerName;
        private String customerPhone;
        private LocalDate depositDate;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal remainingAmount;
        private int daysOverdue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private String type; // WARNING, INFO, DANGER
        private String message;
        private String link;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentInvoice {
        private String invoiceNumber;
        private String customerName;
        private LocalDate depositDate;
        private LocalDate deliveryDate;
        private BigDecimal amount;
        private String processingStatus;
        private boolean paid;
    }
}
