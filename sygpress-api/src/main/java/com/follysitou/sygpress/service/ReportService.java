package com.follysitou.sygpress.service;

import com.follysitou.sygpress.dto.response.CustomerReportResponse;
import com.follysitou.sygpress.dto.response.InvoiceStatusReportResponse;
import com.follysitou.sygpress.dto.response.SalesReportResponse;
import com.follysitou.sygpress.dto.response.UserReportResponse;
import com.follysitou.sygpress.enums.ProcessingStatus;
import com.follysitou.sygpress.model.Invoice;
import com.follysitou.sygpress.model.InvoiceLine;
import com.follysitou.sygpress.model.User;
import com.follysitou.sygpress.repository.InvoiceRepository;
import com.follysitou.sygpress.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SalesReportResponse generateSalesReport(LocalDate startDate, LocalDate endDate) {
        return generateSalesReport(startDate, endDate, null);
    }

    @Transactional(readOnly = true)
    public SalesReportResponse generateSalesReport(LocalDate startDate, LocalDate endDate, String userEmail) {
        List<Invoice> invoices = invoiceRepository.findByDepositDateBetweenAndDeletedFalse(startDate, endDate);

        // Filtrer par utilisateur si spécifié
        if (userEmail != null && !userEmail.isEmpty()) {
            invoices = invoices.stream()
                    .filter(i -> userEmail.equals(i.getCreatedBy()))
                    .collect(Collectors.toList());
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalUnpaid = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        // Daily sales map
        Map<LocalDate, SalesReportResponse.DailySales> dailySalesMap = new TreeMap<>();
        // Service breakdown map
        Map<String, SalesReportResponse.ServiceSales> serviceMap = new HashMap<>();

        for (Invoice invoice : invoices) {
            BigDecimal invoiceTotal = invoice.calculateTotalAmount();
            totalRevenue = totalRevenue.add(invoiceTotal);
            totalPaid = totalPaid.add(invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO);
            totalUnpaid = totalUnpaid.add(invoice.getRemainingAmount() != null ? invoice.getRemainingAmount() : BigDecimal.ZERO);
            totalDiscount = totalDiscount.add(invoice.getDiscount() != null ? invoice.getDiscount() : BigDecimal.ZERO);

            // Daily sales
            LocalDate date = invoice.getDepositDate();
            SalesReportResponse.DailySales dailySales = dailySalesMap.getOrDefault(date,
                    SalesReportResponse.DailySales.builder()
                            .date(date)
                            .invoiceCount(0)
                            .amount(BigDecimal.ZERO)
                            .build());
            dailySales.setInvoiceCount(dailySales.getInvoiceCount() + 1);
            dailySales.setAmount(dailySales.getAmount().add(invoiceTotal));
            dailySalesMap.put(date, dailySales);

            // Service breakdown
            for (InvoiceLine line : invoice.getInvoiceLines()) {
                String serviceName = line.getPricing().getService().getName();
                SalesReportResponse.ServiceSales serviceSales = serviceMap.getOrDefault(serviceName,
                        SalesReportResponse.ServiceSales.builder()
                                .serviceName(serviceName)
                                .quantity(0)
                                .amount(BigDecimal.ZERO)
                                .build());
                serviceSales.setQuantity(serviceSales.getQuantity() + line.getQuantity());
                serviceSales.setAmount(serviceSales.getAmount().add(line.getAmount()));
                serviceMap.put(serviceName, serviceSales);
            }
        }

        BigDecimal averageAmount = invoices.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(invoices.size()), 2, RoundingMode.HALF_UP);

        return SalesReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalInvoices(invoices.size())
                .totalRevenue(totalRevenue)
                .totalPaid(totalPaid)
                .totalUnpaid(totalUnpaid)
                .totalDiscount(totalDiscount)
                .averageInvoiceAmount(averageAmount)
                .dailySales(new ArrayList<>(dailySalesMap.values()))
                .serviceBreakdown(new ArrayList<>(serviceMap.values()))
                .build();
    }

    @Transactional(readOnly = true)
    public CustomerReportResponse generateCustomerReport(LocalDate startDate, LocalDate endDate) {
        return generateCustomerReport(startDate, endDate, null);
    }

    @Transactional(readOnly = true)
    public CustomerReportResponse generateCustomerReport(LocalDate startDate, LocalDate endDate, String userEmail) {
        List<Invoice> invoices = invoiceRepository.findByDepositDateBetweenAndDeletedFalse(startDate, endDate);

        // Filtrer par utilisateur si spécifié
        if (userEmail != null && !userEmail.isEmpty()) {
            invoices = invoices.stream()
                    .filter(i -> userEmail.equals(i.getCreatedBy()))
                    .collect(Collectors.toList());
        }

        // Pour le filtre utilisateur, recalculer les totaux basés sur les factures filtrées
        int totalCustomers = userEmail != null && !userEmail.isEmpty()
                ? (int) invoices.stream().filter(i -> i.getCustomer() != null).map(i -> i.getCustomer().getId()).distinct().count()
                : invoiceRepository.countDistinctCustomersByDateRange(startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        int newCustomers = userEmail != null && !userEmail.isEmpty()
                ? (int) invoices.stream()
                        .filter(i -> i.getCustomer() != null && i.getCustomer().getCreatedAt().isAfter(startDateTime))
                        .map(i -> i.getCustomer().getId())
                        .distinct()
                        .count()
                : invoiceRepository.countNewCustomersByDateRange(startDate, endDate, startDateTime);

        // Group by customer
        Map<Long, List<Invoice>> customerInvoices = invoices.stream()
                .filter(i -> i.getCustomer() != null)
                .collect(Collectors.groupingBy(i -> i.getCustomer().getId()));

        List<CustomerReportResponse.CustomerStats> customerStats = customerInvoices.entrySet().stream()
                .map(entry -> {
                    List<Invoice> custInvoices = entry.getValue();
                    Invoice firstInvoice = custInvoices.get(0);

                    BigDecimal totalSpent = custInvoices.stream()
                            .map(Invoice::calculateTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalUnpaid = custInvoices.stream()
                            .map(i -> i.getRemainingAmount() != null ? i.getRemainingAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return CustomerReportResponse.CustomerStats.builder()
                            .customerName(firstInvoice.getCustomer().getName())
                            .customerPhone(firstInvoice.getCustomer().getPhoneNumber())
                            .invoiceCount(custInvoices.size())
                            .totalSpent(totalSpent)
                            .totalUnpaid(totalUnpaid)
                            .build();
                })
                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                .limit(20)
                .collect(Collectors.toList());

        return CustomerReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalCustomers(totalCustomers)
                .newCustomers(newCustomers)
                .topCustomers(customerStats)
                .build();
    }

    @Transactional(readOnly = true)
    public InvoiceStatusReportResponse generateInvoiceStatusReport(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findByDepositDateBetweenAndDeletedFalse(startDate, endDate);

        int paidInvoices = 0;
        int unpaidInvoices = 0;
        int partiallyPaidInvoices = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalRemaining = BigDecimal.ZERO;

        Map<ProcessingStatus, InvoiceStatusReportResponse.ProcessingStatusCount> statusMap = new EnumMap<>(ProcessingStatus.class);
        List<InvoiceStatusReportResponse.UnpaidInvoice> unpaidList = new ArrayList<>();

        for (Invoice invoice : invoices) {
            BigDecimal invoiceTotal = invoice.calculateTotalAmount();
            BigDecimal paid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal remaining = invoice.getRemainingAmount() != null ? invoice.getRemainingAmount() : BigDecimal.ZERO;

            totalAmount = totalAmount.add(invoiceTotal);
            totalPaid = totalPaid.add(paid);
            totalRemaining = totalRemaining.add(remaining);

            if (invoice.isInvoicePaid()) {
                paidInvoices++;
            } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
                partiallyPaidInvoices++;
                addUnpaidInvoice(unpaidList, invoice, invoiceTotal, remaining);
            } else {
                unpaidInvoices++;
                addUnpaidInvoice(unpaidList, invoice, invoiceTotal, remaining);
            }

            // Processing status breakdown
            ProcessingStatus status = invoice.getProcessingStatus();
            InvoiceStatusReportResponse.ProcessingStatusCount statusCount = statusMap.getOrDefault(status,
                    InvoiceStatusReportResponse.ProcessingStatusCount.builder()
                            .status(status.name())
                            .count(0)
                            .amount(BigDecimal.ZERO)
                            .build());
            statusCount.setCount(statusCount.getCount() + 1);
            statusCount.setAmount(statusCount.getAmount().add(invoiceTotal));
            statusMap.put(status, statusCount);
        }

        return InvoiceStatusReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalInvoices(invoices.size())
                .paidInvoices(paidInvoices)
                .unpaidInvoices(unpaidInvoices)
                .partiallyPaidInvoices(partiallyPaidInvoices)
                .totalAmount(totalAmount)
                .totalPaid(totalPaid)
                .totalRemaining(totalRemaining)
                .processingStatusBreakdown(new ArrayList<>(statusMap.values()))
                .unpaidInvoicesList(unpaidList)
                .build();
    }

    private void addUnpaidInvoice(List<InvoiceStatusReportResponse.UnpaidInvoice> list, Invoice invoice, BigDecimal total, BigDecimal remaining) {
        list.add(InvoiceStatusReportResponse.UnpaidInvoice.builder()
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerName(invoice.getCustomer() != null ? invoice.getCustomer().getName() : "N/A")
                .depositDate(invoice.getDepositDate())
                .totalAmount(total)
                .remainingAmount(remaining)
                .build());
    }

    @Transactional(readOnly = true)
    public UserReportResponse generateUserReport(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findByDepositDateBetweenAndDeletedFalse(startDate, endDate);

        // Calculer le revenu total de la période pour les pourcentages
        BigDecimal totalRevenue = invoices.stream()
                .map(Invoice::calculateTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Regrouper les factures par utilisateur créateur
        Map<String, List<Invoice>> invoicesByUser = invoices.stream()
                .filter(i -> i.getCreatedBy() != null && !i.getCreatedBy().equals("SYSTEM"))
                .collect(Collectors.groupingBy(Invoice::getCreatedBy));

        // Créer un Map pour rechercher les utilisateurs par email
        Map<String, User> usersByEmail = new HashMap<>();
        invoicesByUser.keySet().forEach(email -> {
            userRepository.findByEmail(email).ifPresent(user -> usersByEmail.put(email, user));
        });

        // Calculer les statistiques pour chaque utilisateur
        List<UserReportResponse.UserStats> userStats = invoicesByUser.entrySet().stream()
                .map(entry -> {
                    String userEmail = entry.getKey();
                    List<Invoice> userInvoices = entry.getValue();
                    User user = usersByEmail.get(userEmail);

                    BigDecimal userTotalRevenue = userInvoices.stream()
                            .map(Invoice::calculateTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal userTotalPaid = userInvoices.stream()
                            .map(i -> i.getAmountPaid() != null ? i.getAmountPaid() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal userTotalUnpaid = userInvoices.stream()
                            .map(i -> i.getRemainingAmount() != null ? i.getRemainingAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal averageAmount = userInvoices.isEmpty() ? BigDecimal.ZERO :
                            userTotalRevenue.divide(BigDecimal.valueOf(userInvoices.size()), 2, RoundingMode.HALF_UP);

                    double percentage = totalRevenue.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                            userTotalRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .doubleValue();

                    return UserReportResponse.UserStats.builder()
                            .userId(user != null ? user.getId() : null)
                            .userName(user != null ? user.getLastName() + " " + user.getFirstName() : userEmail)
                            .userEmail(userEmail)
                            .userRole(user != null ? user.getRole().name() : "UNKNOWN")
                            .invoiceCount(userInvoices.size())
                            .totalRevenue(userTotalRevenue)
                            .totalPaid(userTotalPaid)
                            .totalUnpaid(userTotalUnpaid)
                            .averageInvoiceAmount(averageAmount)
                            .percentage(percentage)
                            .build();
                })
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                .collect(Collectors.toList());

        return UserReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalUsers(userStats.size())
                .totalRevenue(totalRevenue)
                .userStats(userStats)
                .build();
    }
}
