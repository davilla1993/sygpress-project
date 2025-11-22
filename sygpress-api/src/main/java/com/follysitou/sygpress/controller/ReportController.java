package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.response.CustomerReportResponse;
import com.follysitou.sygpress.dto.response.InvoiceStatusReportResponse;
import com.follysitou.sygpress.dto.response.SalesReportResponse;
import com.follysitou.sygpress.dto.response.ServiceReportResponse;
import com.follysitou.sygpress.dto.response.UserReportResponse;
import com.follysitou.sygpress.model.User;
import com.follysitou.sygpress.repository.UserRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Génération de rapports")
public class ReportController {

    private final ReportService reportService;
    private final ReportPdfService reportPdfService;
    private final UserRepository userRepository;

    // Sales Report endpoints
    @GetMapping("/sales")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer le rapport des ventes")
    public ResponseEntity<SalesReportResponse> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String userEmail) {
        return ResponseEntity.ok(reportService.generateSalesReport(startDate, endDate, userEmail));
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String userEmail) throws IOException {
        SalesReportResponse report = reportService.generateSalesReport(startDate, endDate, userEmail);
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String userEmail) {
        return ResponseEntity.ok(reportService.generateCustomerReport(startDate, endDate, userEmail));
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String userEmail) throws IOException {
        CustomerReportResponse report = reportService.generateCustomerReport(startDate, endDate, userEmail);
        byte[] pdfContent = reportPdfService.generateCustomerReportPdf(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "rapport-clients-" + startDate + "-" + endDate + ".pdf");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    // Service Report endpoints
    @GetMapping("/services")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer le rapport des services")
    public ResponseEntity<ServiceReportResponse> getServiceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String userEmail) {
        return ResponseEntity.ok(reportService.generateServiceReport(startDate, endDate, userEmail));
    }

    @GetMapping("/services/today")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer le rapport des services d'aujourd'hui")
    public ResponseEntity<ServiceReportResponse> getServiceReportToday() {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(reportService.generateServiceReport(today, today));
    }

    @GetMapping("/services/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Télécharger le rapport des services en PDF")
    public ResponseEntity<byte[]> downloadServiceReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String userEmail) throws IOException {
        ServiceReportResponse report = reportService.generateServiceReport(startDate, endDate, userEmail);
        byte[] pdfContent = reportPdfService.generateServiceReportPdf(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "rapport-services-" + startDate + "-" + endDate + ".pdf");

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

    // User Report endpoints
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer le rapport par utilisateur")
    public ResponseEntity<UserReportResponse> getUserReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateUserReport(startDate, endDate));
    }

    @GetMapping("/users/today")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Générer le rapport par utilisateur d'aujourd'hui")
    public ResponseEntity<UserReportResponse> getUserReportToday() {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(reportService.generateUserReport(today, today));
    }

    @GetMapping("/users/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Télécharger le rapport par utilisateur en PDF")
    public ResponseEntity<byte[]> downloadUserReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
        UserReportResponse report = reportService.generateUserReport(startDate, endDate);
        byte[] pdfContent = reportPdfService.generateUserReportPdf(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "rapport-utilisateurs-" + startDate + "-" + endDate + ".pdf");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    // Helper endpoint to get list of users for filtering
    @GetMapping("/users/list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer la liste des utilisateurs actifs")
    public ResponseEntity<List<UserInfo>> getActiveUsers() {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !u.getDeleted() && u.getEnabled())
                .collect(Collectors.toList());

        List<UserInfo> userInfos = users.stream()
                .map(u -> new UserInfo(u.getEmail(), u.getLastName() + " " + u.getFirstName(), u.getRole().name()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userInfos);
    }

    // Simple DTO for user info
    public record UserInfo(String email, String name, String role) {}
}
