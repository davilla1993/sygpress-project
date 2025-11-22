package com.follysitou.sygpress.service;

import com.follysitou.sygpress.dto.response.CustomerReportResponse;
import com.follysitou.sygpress.dto.response.InvoiceStatusReportResponse;
import com.follysitou.sygpress.dto.response.SalesReportResponse;
import com.follysitou.sygpress.dto.response.ServiceReportResponse;
import com.follysitou.sygpress.enums.ProcessingStatus;
import com.follysitou.sygpress.model.Invoice;
import com.follysitou.sygpress.model.InvoiceLine;
import com.follysitou.sygpress.repository.InvoiceRepository;
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

    @Transactional(readOnly = true)
    public SalesReportResponse generateSalesReport(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findByDepositDateBetweenAndDeletedFalse(startDate, endDate);

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
        List<Invoice> invoices = invoiceRepository.findByDepositDateBetweenAndDeletedFalse(startDate, endDate);

        int totalCustomers = invoiceRepository.countDistinctCustomersByDateRange(startDate, endDate);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        int newCustomers = invoiceRepository.countNewCustomersByDateRange(startDate, endDate, startDateTime);

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
    public ServiceReportResponse generateServiceReport(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findByDepositDateBetweenAndDeletedFalse(startDate, endDate);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        Map<String, ServiceReportResponse.ServiceStats> serviceMap = new HashMap<>();
        Map<String, ServiceReportResponse.ArticleStats> articleMap = new HashMap<>();
        Map<String, ServiceReportResponse.CombinationStats> combinationMap = new HashMap<>();

        // Analyse de toutes les lignes de facture
        for (Invoice invoice : invoices) {
            for (InvoiceLine line : invoice.getInvoiceLines()) {
                BigDecimal lineAmount = line.getAmount();
                totalRevenue = totalRevenue.add(lineAmount);

                String serviceName = line.getPricing().getService().getName();
                String articleName = line.getPricing().getArticle().getName();
                int quantity = line.getQuantity();

                // Statistiques par service
                ServiceReportResponse.ServiceStats serviceStats = serviceMap.getOrDefault(serviceName,
                        ServiceReportResponse.ServiceStats.builder()
                                .serviceName(serviceName)
                                .quantity(0)
                                .revenue(BigDecimal.ZERO)
                                .percentage(BigDecimal.ZERO)
                                .build());
                serviceStats.setQuantity(serviceStats.getQuantity() + quantity);
                serviceStats.setRevenue(serviceStats.getRevenue().add(lineAmount));
                serviceMap.put(serviceName, serviceStats);

                // Statistiques par article
                ServiceReportResponse.ArticleStats articleStats = articleMap.getOrDefault(articleName,
                        ServiceReportResponse.ArticleStats.builder()
                                .articleName(articleName)
                                .quantity(0)
                                .revenue(BigDecimal.ZERO)
                                .percentage(BigDecimal.ZERO)
                                .build());
                articleStats.setQuantity(articleStats.getQuantity() + quantity);
                articleStats.setRevenue(articleStats.getRevenue().add(lineAmount));
                articleMap.put(articleName, articleStats);

                // Statistiques par combinaison article + service
                String combinationKey = articleName + "|" + serviceName;
                ServiceReportResponse.CombinationStats combinationStats = combinationMap.getOrDefault(combinationKey,
                        ServiceReportResponse.CombinationStats.builder()
                                .articleName(articleName)
                                .serviceName(serviceName)
                                .quantity(0)
                                .revenue(BigDecimal.ZERO)
                                .build());
                combinationStats.setQuantity(combinationStats.getQuantity() + quantity);
                combinationStats.setRevenue(combinationStats.getRevenue().add(lineAmount));
                combinationMap.put(combinationKey, combinationStats);
            }
        }

        // Calculer les pourcentages
        final BigDecimal finalTotalRevenue = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? totalRevenue : BigDecimal.ONE;

        // Pourcentages par service
        serviceMap.values().forEach(stats -> {
            BigDecimal percentage = stats.getRevenue()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(finalTotalRevenue, 2, RoundingMode.HALF_UP);
            stats.setPercentage(percentage);
        });

        // Pourcentages par article
        articleMap.values().forEach(stats -> {
            BigDecimal percentage = stats.getRevenue()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(finalTotalRevenue, 2, RoundingMode.HALF_UP);
            stats.setPercentage(percentage);
        });

        // Trier les résultats
        List<ServiceReportResponse.ServiceStats> sortedServices = serviceMap.values().stream()
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .collect(Collectors.toList());

        List<ServiceReportResponse.ArticleStats> sortedArticles = articleMap.values().stream()
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .collect(Collectors.toList());

        List<ServiceReportResponse.CombinationStats> sortedCombinations = combinationMap.values().stream()
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(20) // Top 20 combinaisons
                .collect(Collectors.toList());

        return ServiceReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalServices(sortedServices.size())
                .totalRevenue(totalRevenue)
                .serviceStats(sortedServices)
                .articleStats(sortedArticles)
                .combinationStats(sortedCombinations)
                .build();
    }
}
