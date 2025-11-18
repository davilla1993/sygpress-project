package com.follysitou.sygpress.service;

import com.follysitou.sygpress.dto.response.AdminDashboardResponse;
import com.follysitou.sygpress.dto.response.UserDashboardResponse;
import com.follysitou.sygpress.enums.ProcessingStatus;
import com.follysitou.sygpress.model.Invoice;
import com.follysitou.sygpress.model.InvoiceLine;
import com.follysitou.sygpress.repository.CustomerRepository;
import com.follysitou.sygpress.repository.InvoiceRepository;
import com.follysitou.sygpress.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate weekAgo = today.minusDays(6);

        // Statistiques globales
        int totalCustomers = (int) customerRepository.count();
        int totalInvoices = invoiceRepository.countAllInvoices();
        int totalUsers = (int) userRepository.count();

        // Calcul du revenu total
        List<Invoice> allInvoices = invoiceRepository.findAllOrderByCreatedAtDesc();
        BigDecimal totalRevenue = allInvoices.stream()
                .map(Invoice::calculateTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = invoiceRepository.sumAllPaidAmount();
        BigDecimal totalUnpaid = totalRevenue.subtract(totalPaid);
        double paymentRate = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalPaid.divide(totalRevenue, 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0;

        // Statistiques du jour
        List<Invoice> todayInvoices = invoiceRepository.findByDepositDateAndDeletedFalse(today);
        BigDecimal todayRevenue = todayInvoices.stream()
                .map(Invoice::calculateTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal todayPayments = todayInvoices.stream()
                .map(i -> i.getAmountPaid() != null ? i.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int todayNewCustomers = invoiceRepository.countNewCustomersByDateRange(today, today, today.atStartOfDay());

        // Statistiques du mois
        List<Invoice> monthInvoices = invoiceRepository.findByDepositDateBetweenAndDeletedFalse(monthStart, today);
        BigDecimal monthRevenue = monthInvoices.stream()
                .map(Invoice::calculateTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthPayments = monthInvoices.stream()
                .map(i -> i.getAmountPaid() != null ? i.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int monthNewCustomers = invoiceRepository.countNewCustomersByDateRange(monthStart, today, monthStart.atStartOfDay());

        // Répartition par statut de traitement
        List<AdminDashboardResponse.ProcessingStatusStat> processingStatusStats = new ArrayList<>();
        for (ProcessingStatus status : ProcessingStatus.values()) {
            List<Invoice> statusInvoices = invoiceRepository.findByProcessingStatusAndDeletedFalse(status);
            BigDecimal statusAmount = statusInvoices.stream()
                    .map(Invoice::calculateTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            processingStatusStats.add(AdminDashboardResponse.ProcessingStatusStat.builder()
                    .status(status.name())
                    .count(statusInvoices.size())
                    .amount(statusAmount)
                    .build());
        }

        // Évolution des ventes (7 derniers jours)
        List<AdminDashboardResponse.DailyStat> last7DaysSales = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<Invoice> dayInvoices = invoiceRepository.findByDepositDateAndDeletedFalse(date);
            BigDecimal dayRevenue = dayInvoices.stream()
                    .map(Invoice::calculateTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            last7DaysSales.add(AdminDashboardResponse.DailyStat.builder()
                    .date(date)
                    .invoiceCount(dayInvoices.size())
                    .revenue(dayRevenue)
                    .build());
        }

        // Évolution des ventes (12 derniers mois)
        List<AdminDashboardResponse.MonthlyStat> last12MonthsSales = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            List<Invoice> monthlyInvoices = invoiceRepository.findByDepositDateBetweenAndDeletedFalse(start, end);
            BigDecimal monthlyRevenue = monthlyInvoices.stream()
                    .map(Invoice::calculateTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            last12MonthsSales.add(AdminDashboardResponse.MonthlyStat.builder()
                    .month(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH))
                    .year(ym.getYear())
                    .invoiceCount(monthlyInvoices.size())
                    .revenue(monthlyRevenue)
                    .build());
        }

        // Top 5 clients
        Map<Long, List<Invoice>> customerInvoicesMap = allInvoices.stream()
                .filter(i -> i.getCustomer() != null)
                .collect(Collectors.groupingBy(i -> i.getCustomer().getId()));

        List<AdminDashboardResponse.TopCustomer> topCustomers = customerInvoicesMap.entrySet().stream()
                .map(entry -> {
                    List<Invoice> custInvoices = entry.getValue();
                    Invoice first = custInvoices.get(0);
                    BigDecimal total = custInvoices.stream()
                            .map(Invoice::calculateTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return AdminDashboardResponse.TopCustomer.builder()
                            .name(first.getCustomer().getName())
                            .phone(first.getCustomer().getPhoneNumber())
                            .invoiceCount(custInvoices.size())
                            .totalSpent(total)
                            .build();
                })
                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                .limit(5)
                .collect(Collectors.toList());

        // Top 5 services
        Map<String, AdminDashboardResponse.TopService> serviceMap = new HashMap<>();
        for (Invoice invoice : allInvoices) {
            for (InvoiceLine line : invoice.getInvoiceLines()) {
                String serviceName = line.getPricing().getService().getName();
                AdminDashboardResponse.TopService service = serviceMap.getOrDefault(serviceName,
                        AdminDashboardResponse.TopService.builder()
                                .serviceName(serviceName)
                                .quantity(0)
                                .revenue(BigDecimal.ZERO)
                                .build());
                service.setQuantity(service.getQuantity() + line.getQuantity());
                service.setRevenue(service.getRevenue().add(line.getAmount()));
                serviceMap.put(serviceName, service);
            }
        }

        List<AdminDashboardResponse.TopService> topServices = serviceMap.values().stream()
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(5)
                .collect(Collectors.toList());

        // Factures récentes (10 dernières)
        List<AdminDashboardResponse.RecentInvoice> recentInvoices = allInvoices.stream()
                .limit(10)
                .map(invoice -> AdminDashboardResponse.RecentInvoice.builder()
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .customerName(invoice.getCustomer() != null ? invoice.getCustomer().getName() : "N/A")
                        .depositDate(invoice.getDepositDate())
                        .amount(invoice.calculateTotalAmount())
                        .status(invoice.getProcessingStatus().name())
                        .paid(invoice.isInvoicePaid())
                        .build())
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalCustomers(totalCustomers)
                .totalInvoices(totalInvoices)
                .totalUsers(totalUsers)
                .totalRevenue(totalRevenue)
                .todayInvoices(todayInvoices.size())
                .todayRevenue(todayRevenue)
                .todayPayments(todayPayments)
                .todayNewCustomers(todayNewCustomers)
                .monthInvoices(monthInvoices.size())
                .monthRevenue(monthRevenue)
                .monthPayments(monthPayments)
                .monthNewCustomers(monthNewCustomers)
                .totalPaid(totalPaid)
                .totalUnpaid(totalUnpaid)
                .paymentRate(paymentRate)
                .processingStatusStats(processingStatusStats)
                .last7DaysSales(last7DaysSales)
                .last12MonthsSales(last12MonthsSales)
                .topCustomers(topCustomers)
                .topServices(topServices)
                .recentInvoices(recentInvoices)
                .build();
    }

    @Transactional(readOnly = true)
    public UserDashboardResponse getUserDashboard() {
        LocalDate today = LocalDate.now();

        // Statistiques du jour
        List<Invoice> todayInvoices = invoiceRepository.findByDepositDateAndDeletedFalse(today);
        BigDecimal todayRevenue = todayInvoices.stream()
                .map(Invoice::calculateTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal todayPayments = todayInvoices.stream()
                .map(i -> i.getAmountPaid() != null ? i.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Livraisons du jour
        List<Invoice> deliveriesTodayList = invoiceRepository.findByDeliveryDateAndDeletedFalse(today);
        int todayDeliveries = deliveriesTodayList.size();

        // En-cours par statut de traitement
        List<UserDashboardResponse.ProcessingQueue> processingQueues = new ArrayList<>();
        for (ProcessingStatus status : ProcessingStatus.values()) {
            int count = invoiceRepository.countByProcessingStatusAndDeletedFalse(status);
            processingQueues.add(UserDashboardResponse.ProcessingQueue.builder()
                    .status(status.name())
                    .statusLabel(getStatusLabel(status))
                    .count(count)
                    .build());
        }

        // Factures à livrer aujourd'hui
        List<UserDashboardResponse.DeliveryToday> deliveriesToday = deliveriesTodayList.stream()
                .map(invoice -> UserDashboardResponse.DeliveryToday.builder()
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .customerName(invoice.getCustomer() != null ? invoice.getCustomer().getName() : "N/A")
                        .customerPhone(invoice.getCustomer() != null ? invoice.getCustomer().getPhoneNumber() : "N/A")
                        .amount(invoice.calculateTotalAmount())
                        .remainingAmount(invoice.getRemainingAmount())
                        .processingStatus(invoice.getProcessingStatus().name())
                        .ready(invoice.getProcessingStatus() == ProcessingStatus.LIVRE)
                        .build())
                .collect(Collectors.toList());

        // Factures en attente de paiement
        List<Invoice> unpaidInvoices = invoiceRepository.findAllUnpaidInvoices();
        List<UserDashboardResponse.PendingPayment> pendingPayments = unpaidInvoices.stream()
                .limit(20)
                .map(invoice -> {
                    long daysOverdue = ChronoUnit.DAYS.between(invoice.getDeliveryDate(), today);
                    return UserDashboardResponse.PendingPayment.builder()
                            .invoiceNumber(invoice.getInvoiceNumber())
                            .customerName(invoice.getCustomer() != null ? invoice.getCustomer().getName() : "N/A")
                            .customerPhone(invoice.getCustomer() != null ? invoice.getCustomer().getPhoneNumber() : "N/A")
                            .depositDate(invoice.getDepositDate())
                            .totalAmount(invoice.calculateTotalAmount())
                            .paidAmount(invoice.getAmountPaid())
                            .remainingAmount(invoice.getRemainingAmount())
                            .daysOverdue((int) Math.max(0, daysOverdue))
                            .build();
                })
                .collect(Collectors.toList());

        // Alertes
        List<UserDashboardResponse.Alert> alerts = new ArrayList<>();

        // Alerte: Factures en retard de livraison
        long overdueDeliveries = deliveriesTodayList.stream()
                .filter(i -> i.getProcessingStatus() != ProcessingStatus.LIVRE)
                .count();
        if (overdueDeliveries > 0) {
            alerts.add(UserDashboardResponse.Alert.builder()
                    .type("WARNING")
                    .message(overdueDeliveries + " facture(s) à livrer aujourd'hui non terminée(s)")
                    .link("/invoices?deliveryDate=" + today)
                    .build());
        }

        // Alerte: Factures impayées depuis plus de 7 jours
        long oldUnpaid = unpaidInvoices.stream()
                .filter(i -> ChronoUnit.DAYS.between(i.getDeliveryDate(), today) > 7)
                .count();
        if (oldUnpaid > 0) {
            alerts.add(UserDashboardResponse.Alert.builder()
                    .type("DANGER")
                    .message(oldUnpaid + " facture(s) impayée(s) depuis plus de 7 jours")
                    .link("/invoices?unpaid=true")
                    .build());
        }

        // Info: Nombre de factures en traitement
        int inProcessing = invoiceRepository.countByProcessingStatusAndDeletedFalse(ProcessingStatus.EN_LAVAGE)
                + invoiceRepository.countByProcessingStatusAndDeletedFalse(ProcessingStatus.EN_REPASSAGE);
        if (inProcessing > 0) {
            alerts.add(UserDashboardResponse.Alert.builder()
                    .type("INFO")
                    .message(inProcessing + " article(s) en cours de traitement")
                    .link("/invoices?status=EN_LAVAGE,EN_REPASSAGE")
                    .build());
        }

        // Dernières factures créées (10)
        List<Invoice> allInvoices = invoiceRepository.findAllOrderByCreatedAtDesc();
        List<UserDashboardResponse.RecentInvoice> recentInvoices = allInvoices.stream()
                .limit(10)
                .map(invoice -> UserDashboardResponse.RecentInvoice.builder()
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .customerName(invoice.getCustomer() != null ? invoice.getCustomer().getName() : "N/A")
                        .depositDate(invoice.getDepositDate())
                        .deliveryDate(invoice.getDeliveryDate())
                        .amount(invoice.calculateTotalAmount())
                        .processingStatus(invoice.getProcessingStatus().name())
                        .paid(invoice.isInvoicePaid())
                        .build())
                .collect(Collectors.toList());

        return UserDashboardResponse.builder()
                .todayInvoices(todayInvoices.size())
                .todayRevenue(todayRevenue)
                .todayPayments(todayPayments)
                .todayDeliveries(todayDeliveries)
                .processingQueues(processingQueues)
                .deliveriesToday(deliveriesToday)
                .pendingPayments(pendingPayments)
                .alerts(alerts)
                .recentInvoices(recentInvoices)
                .build();
    }

    private String getStatusLabel(ProcessingStatus status) {
        return switch (status) {
            case COLLECTE -> "Collecte";
            case EN_LAVAGE -> "En Lavage";
            case EN_REPASSAGE -> "En Repassage";
            case PRET -> "Prêt";
            case LIVRE -> "Livré";
            case RECUPERE -> "Récupéré";
        };
    }
}
