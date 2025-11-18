package com.follysitou.sygpress.service;

import com.follysitou.sygpress.dto.response.CustomerReportResponse;
import com.follysitou.sygpress.dto.response.InvoiceStatusReportResponse;
import com.follysitou.sygpress.dto.response.SalesReportResponse;
import com.follysitou.sygpress.model.Company;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportPdfService {

    private final CompanyService companyService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color PRIMARY_COLOR = new Color(44, 62, 80);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color LIGHT_GRAY = new Color(245, 245, 245);

    public byte[] generateSalesReportPdf(SalesReportResponse report) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addReportHeader(document, "Rapport des Ventes", report.getStartDate().format(DATE_FORMATTER), report.getEndDate().format(DATE_FORMATTER));

            // Summary table
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(20);

            addSummaryRow(summaryTable, "Nombre de factures", String.valueOf(report.getTotalInvoices()));
            addSummaryRow(summaryTable, "Chiffre d'affaires total", formatMoney(report.getTotalRevenue()));
            addSummaryRow(summaryTable, "Total encaissé", formatMoney(report.getTotalPaid()));
            addSummaryRow(summaryTable, "Total impayé", formatMoney(report.getTotalUnpaid()));
            addSummaryRow(summaryTable, "Total remises", formatMoney(report.getTotalDiscount()));
            addSummaryRow(summaryTable, "Panier moyen", formatMoney(report.getAverageInvoiceAmount()));

            document.add(summaryTable);

            // Service breakdown
            if (report.getServiceBreakdown() != null && !report.getServiceBreakdown().isEmpty()) {
                document.add(new Paragraph("\n"));
                addSectionTitle(document, "Répartition par service");

                PdfPTable serviceTable = new PdfPTable(3);
                serviceTable.setWidthPercentage(100);
                serviceTable.setWidths(new float[]{3, 1, 2});

                addTableHeader(serviceTable, "Service", "Quantité", "Montant");

                for (SalesReportResponse.ServiceSales service : report.getServiceBreakdown()) {
                    addTableCell(serviceTable, service.getServiceName(), Element.ALIGN_LEFT);
                    addTableCell(serviceTable, String.valueOf(service.getQuantity()), Element.ALIGN_CENTER);
                    addTableCell(serviceTable, formatMoney(service.getAmount()), Element.ALIGN_RIGHT);
                }

                document.add(serviceTable);
            }

            // Daily sales
            if (report.getDailySales() != null && !report.getDailySales().isEmpty()) {
                document.add(new Paragraph("\n"));
                addSectionTitle(document, "Ventes journalières");

                PdfPTable dailyTable = new PdfPTable(3);
                dailyTable.setWidthPercentage(100);
                dailyTable.setWidths(new float[]{2, 1, 2});

                addTableHeader(dailyTable, "Date", "Factures", "Montant");

                for (SalesReportResponse.DailySales daily : report.getDailySales()) {
                    addTableCell(dailyTable, daily.getDate().format(DATE_FORMATTER), Element.ALIGN_LEFT);
                    addTableCell(dailyTable, String.valueOf(daily.getInvoiceCount()), Element.ALIGN_CENTER);
                    addTableCell(dailyTable, formatMoney(daily.getAmount()), Element.ALIGN_RIGHT);
                }

                document.add(dailyTable);
            }

        } catch (DocumentException e) {
            throw new IOException("Erreur lors de la génération du PDF", e);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    public byte[] generateCustomerReportPdf(CustomerReportResponse report) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addReportHeader(document, "Rapport Clients", report.getStartDate().format(DATE_FORMATTER), report.getEndDate().format(DATE_FORMATTER));

            // Summary
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(20);

            addSummaryRow(summaryTable, "Total clients actifs", String.valueOf(report.getTotalCustomers()));
            addSummaryRow(summaryTable, "Nouveaux clients", String.valueOf(report.getNewCustomers()));

            document.add(summaryTable);

            // Top customers
            if (report.getTopCustomers() != null && !report.getTopCustomers().isEmpty()) {
                document.add(new Paragraph("\n"));
                addSectionTitle(document, "Meilleurs clients");

                PdfPTable customersTable = new PdfPTable(5);
                customersTable.setWidthPercentage(100);
                customersTable.setWidths(new float[]{3, 2, 1, 2, 2});

                addTableHeader(customersTable, "Client", "Téléphone", "Factures", "Total dépensé", "Impayé");

                for (CustomerReportResponse.CustomerStats customer : report.getTopCustomers()) {
                    addTableCell(customersTable, customer.getCustomerName(), Element.ALIGN_LEFT);
                    addTableCell(customersTable, customer.getCustomerPhone() != null ? customer.getCustomerPhone() : "-", Element.ALIGN_LEFT);
                    addTableCell(customersTable, String.valueOf(customer.getInvoiceCount()), Element.ALIGN_CENTER);
                    addTableCell(customersTable, formatMoney(customer.getTotalSpent()), Element.ALIGN_RIGHT);
                    addTableCell(customersTable, formatMoney(customer.getTotalUnpaid()), Element.ALIGN_RIGHT);
                }

                document.add(customersTable);
            }

        } catch (DocumentException e) {
            throw new IOException("Erreur lors de la génération du PDF", e);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    public byte[] generateInvoiceStatusReportPdf(InvoiceStatusReportResponse report) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addReportHeader(document, "Rapport État des Factures", report.getStartDate().format(DATE_FORMATTER), report.getEndDate().format(DATE_FORMATTER));

            // Summary
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(20);

            addSummaryRow(summaryTable, "Total factures", String.valueOf(report.getTotalInvoices()));
            addSummaryRow(summaryTable, "Factures payées", String.valueOf(report.getPaidInvoices()));
            addSummaryRow(summaryTable, "Factures partiellement payées", String.valueOf(report.getPartiallyPaidInvoices()));
            addSummaryRow(summaryTable, "Factures impayées", String.valueOf(report.getUnpaidInvoices()));
            addSummaryRow(summaryTable, "Montant total", formatMoney(report.getTotalAmount()));
            addSummaryRow(summaryTable, "Total encaissé", formatMoney(report.getTotalPaid()));
            addSummaryRow(summaryTable, "Total à recouvrer", formatMoney(report.getTotalRemaining()));

            document.add(summaryTable);

            // Processing status breakdown
            if (report.getProcessingStatusBreakdown() != null && !report.getProcessingStatusBreakdown().isEmpty()) {
                document.add(new Paragraph("\n"));
                addSectionTitle(document, "Répartition par état de traitement");

                PdfPTable statusTable = new PdfPTable(3);
                statusTable.setWidthPercentage(100);
                statusTable.setWidths(new float[]{3, 1, 2});

                addTableHeader(statusTable, "État", "Nombre", "Montant");

                for (InvoiceStatusReportResponse.ProcessingStatusCount status : report.getProcessingStatusBreakdown()) {
                    addTableCell(statusTable, status.getStatus(), Element.ALIGN_LEFT);
                    addTableCell(statusTable, String.valueOf(status.getCount()), Element.ALIGN_CENTER);
                    addTableCell(statusTable, formatMoney(status.getAmount()), Element.ALIGN_RIGHT);
                }

                document.add(statusTable);
            }

            // Unpaid invoices list
            if (report.getUnpaidInvoicesList() != null && !report.getUnpaidInvoicesList().isEmpty()) {
                document.add(new Paragraph("\n"));
                addSectionTitle(document, "Liste des factures impayées");

                PdfPTable unpaidTable = new PdfPTable(5);
                unpaidTable.setWidthPercentage(100);
                unpaidTable.setWidths(new float[]{2, 3, 2, 2, 2});

                addTableHeader(unpaidTable, "N° Facture", "Client", "Date", "Montant", "Reste");

                for (InvoiceStatusReportResponse.UnpaidInvoice unpaid : report.getUnpaidInvoicesList()) {
                    addTableCell(unpaidTable, unpaid.getInvoiceNumber(), Element.ALIGN_LEFT);
                    addTableCell(unpaidTable, unpaid.getCustomerName(), Element.ALIGN_LEFT);
                    addTableCell(unpaidTable, unpaid.getDepositDate().format(DATE_FORMATTER), Element.ALIGN_CENTER);
                    addTableCell(unpaidTable, formatMoney(unpaid.getTotalAmount()), Element.ALIGN_RIGHT);
                    addTableCell(unpaidTable, formatMoney(unpaid.getRemainingAmount()), Element.ALIGN_RIGHT);
                }

                document.add(unpaidTable);
            }

        } catch (DocumentException e) {
            throw new IOException("Erreur lors de la génération du PDF", e);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private void addReportHeader(Document document, String title, String startDate, String endDate) throws DocumentException {
        Optional<Company> companyOpt = companyService.getCompany();

        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            Font companyFont = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
            Paragraph companyName = new Paragraph(company.getName(), companyFont);
            companyName.setAlignment(Element.ALIGN_CENTER);
            document.add(companyName);
        }

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, SECONDARY_COLOR);
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingBefore(10);
        document.add(titlePara);

        Font dateFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
        Paragraph datePara = new Paragraph("Période: " + startDate + " - " + endDate, dateFont);
        datePara.setAlignment(Element.ALIGN_CENTER);
        datePara.setSpacingAfter(10);
        document.add(datePara);
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR);
        Paragraph section = new Paragraph(title, sectionFont);
        section.setSpacingBefore(10);
        section.setSpacingAfter(10);
        document.add(section);
    }

    private void addSummaryRow(PdfPTable table, String label, String value) {
        Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.BOLD);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setPadding(8);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addTableCell(PdfPTable table, String text, int alignment) {
        Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL);
        PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0 FCFA";
        return String.format("%,.0f FCFA", amount);
    }
}
