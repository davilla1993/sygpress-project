package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.response.CustomerReportResponse;
import com.follysitou.sygpress.dto.response.InvoiceStatusReportResponse;
import com.follysitou.sygpress.dto.response.SalesReportResponse;
import com.follysitou.sygpress.service.ReportPdfService;
import com.follysitou.sygpress.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Génération de rapports")
public class ReportController {

    private final ReportService reportService;
    private final ReportPdfService reportPdfService;

    // Sales Report endpoints
    @GetMapping("/sales")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer le rapport des ventes")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateSalesReport(startDate, endDate));
    }

    @GetMapping("/sales/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Générer le rapport des ventes d'aujourd'hui")
    public ResponseEntity<SalesReportResponse> getSalesReportToday() {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(reportService.generateSalesReport(today, today));
    }

    @GetMapping("/sales/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Télécharger le rapport des ventes en PDF")
    public ResponseEntity<byte[]> downloadSalesReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        SalesReportResponse report = reportService.generateSalesReport(startDate, endDate);
        byte[] pdfContent = reportPdfService.generateSalesReportPdf(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "rapport-ventes-" + startDate + "-" + endDate + ".pdf");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    // Customer Report endpoints
    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer le rapport clients")
    public ResponseEntity<CustomerReportResponse> getCustomerReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateCustomerReport(startDate, endDate));
    }

    @GetMapping("/customers/today")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer le rapport clients d'aujourd'hui")
    public ResponseEntity<CustomerReportResponse> getCustomerReportToday() {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(reportService.generateCustomerReport(today, today));
    }

    @GetMapping("/customers/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Télécharger le rapport clients en PDF")
    public ResponseEntity<byte[]> downloadCustomerReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        CustomerReportResponse report = reportService.generateCustomerReport(startDate, endDate);
        byte[] pdfContent = reportPdfService.generateCustomerReportPdf(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "rapport-clients-" + startDate + "-" + endDate + ".pdf");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    // Invoice Status Report endpoints
    @GetMapping("/invoices-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer le rapport état des factures")
    public ResponseEntity<InvoiceStatusReportResponse> getInvoiceStatusReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateInvoiceStatusReport(startDate, endDate));
    }

    @GetMapping("/invoices-status/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Générer le rapport état des factures d'aujourd'hui")
    public ResponseEntity<InvoiceStatusReportResponse> getInvoiceStatusReportToday() {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(reportService.generateInvoiceStatusReport(today, today));
    }

    @GetMapping("/invoices-status/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Télécharger le rapport état des factures en PDF")
    public ResponseEntity<byte[]> downloadInvoiceStatusReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        InvoiceStatusReportResponse report = reportService.generateInvoiceStatusReport(startDate, endDate);
        byte[] pdfContent = reportPdfService.generateInvoiceStatusReportPdf(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "rapport-factures-" + startDate + "-" + endDate + ".pdf");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
}
