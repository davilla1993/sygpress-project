package com.follysitou.sygpress.service;

import com.follysitou.sygpress.model.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private final CompanyService companyService;
    private final InvoiceService invoiceService;
    private final FileStorageService fileStorageService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color PRIMARY_COLOR = new Color(44, 62, 80);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color LIGHT_GRAY = new Color(245, 245, 245);

    public byte[] generateInvoicePdf(String invoicePublicId) throws IOException {
        Invoice invoice = invoiceService.findByPublicId(invoicePublicId);
        Optional<Company> companyOpt = companyService.getCompany();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add company header
            companyOpt.ifPresent(company -> addCompanyHeader(document, company));

            // Add invoice title
            addInvoiceTitle(document, invoice);

            // Add customer info
            addCustomerInfo(document, invoice);

            // Add invoice details table
            addInvoiceDetails(document, invoice);

            // Add totals
            addTotals(document, invoice);

            // Add footer
            companyOpt.ifPresent(company -> addFooter(document, company));

        } catch (DocumentException e) {
            throw new IOException("Erreur lors de la génération du PDF", e);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private void addCompanyHeader(Document document, Company company) {
        try {
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 2});

            // Logo cell
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            if (company.getLogoPath() != null && !company.getLogoPath().isEmpty()) {
                try {
                    Resource logoResource = fileStorageService.loadFileAsResource(company.getLogoPath());
                    byte[] logoBytes = logoResource.getInputStream().readAllBytes();
                    Image logo = Image.getInstance(logoBytes);
                    logo.scaleToFit(100, 80);
                    logoCell.addElement(logo);
                } catch (Exception e) {
                    // Logo loading failed, skip it
                }
            }
            headerTable.addCell(logoCell);

            // Company info cell
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Font companyNameFont = new Font(Font.HELVETICA, 16, Font.BOLD, PRIMARY_COLOR);
            Font infoFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);

            Paragraph companyName = new Paragraph(company.getName(), companyNameFont);
            companyName.setAlignment(Element.ALIGN_RIGHT);
            infoCell.addElement(companyName);

            if (company.getAddress() != null) {
                Paragraph address = new Paragraph(company.getAddress(), infoFont);
                address.setAlignment(Element.ALIGN_RIGHT);
                infoCell.addElement(address);
            }

            if (company.getCity() != null) {
                Paragraph city = new Paragraph(company.getCity(), infoFont);
                city.setAlignment(Element.ALIGN_RIGHT);
                infoCell.addElement(city);
            }

            if (company.getPhoneNumber() != null) {
                Paragraph phone = new Paragraph("Tél: " + company.getPhoneNumber(), infoFont);
                phone.setAlignment(Element.ALIGN_RIGHT);
                infoCell.addElement(phone);
            }

            if (company.getEmail() != null) {
                Paragraph email = new Paragraph(company.getEmail(), infoFont);
                email.setAlignment(Element.ALIGN_RIGHT);
                infoCell.addElement(email);
            }

            headerTable.addCell(infoCell);
            document.add(headerTable);
            document.add(new Paragraph("\n"));

        } catch (DocumentException e) {
            // Skip header if error
        }
    }

    private void addInvoiceTitle(Document document, Invoice invoice) {
        try {
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, SECONDARY_COLOR);
            Paragraph title = new Paragraph("FACTURE N° " + invoice.getInvoiceNumber(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Dates
            PdfPTable datesTable = new PdfPTable(2);
            datesTable.setWidthPercentage(100);

            Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
            Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            PdfPCell depositCell = new PdfPCell();
            depositCell.setBorder(Rectangle.NO_BORDER);
            depositCell.addElement(new Phrase("Date de dépôt: ", labelFont));
            depositCell.addElement(new Phrase(invoice.getDepositDate().format(DATE_FORMATTER), valueFont));
            datesTable.addCell(depositCell);

            PdfPCell deliveryCell = new PdfPCell();
            deliveryCell.setBorder(Rectangle.NO_BORDER);
            deliveryCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph deliveryLabel = new Paragraph("Date de livraison: ", labelFont);
            deliveryLabel.setAlignment(Element.ALIGN_RIGHT);
            deliveryCell.addElement(deliveryLabel);
            Paragraph deliveryValue = new Paragraph(invoice.getDeliveryDate().format(DATE_FORMATTER), valueFont);
            deliveryValue.setAlignment(Element.ALIGN_RIGHT);
            deliveryCell.addElement(deliveryValue);
            datesTable.addCell(deliveryCell);

            document.add(datesTable);
            document.add(new Paragraph("\n"));

        } catch (DocumentException e) {
            // Skip title if error
        }
    }

    private void addCustomerInfo(Document document, Invoice invoice) {
        try {
            Customer customer = invoice.getCustomer();
            if (customer == null) return;

            PdfPTable customerTable = new PdfPTable(1);
            customerTable.setWidthPercentage(50);
            customerTable.setHorizontalAlignment(Element.ALIGN_LEFT);

            Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD, PRIMARY_COLOR);
            Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(LIGHT_GRAY);
            cell.setPadding(10);
            cell.setBorderColor(Color.LIGHT_GRAY);

            cell.addElement(new Phrase("CLIENT", labelFont));
            cell.addElement(new Phrase(customer.getName(), valueFont));

            if (customer.getPhoneNumber() != null) {
                cell.addElement(new Phrase("Tél: " + customer.getPhoneNumber(), valueFont));
            }

            if (customer.getAddress() != null) {
                cell.addElement(new Phrase(customer.getAddress(), valueFont));
            }

            customerTable.addCell(cell);
            document.add(customerTable);
            document.add(new Paragraph("\n"));

        } catch (DocumentException e) {
            // Skip customer info if error
        }
    }

    private void addInvoiceDetails(Document document, Invoice invoice) {
        try {
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 1, 2, 2});

            // Header
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            String[] headers = {"Article", "Service", "Qté", "Prix Unit.", "Montant"};

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(PRIMARY_COLOR);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Invoice lines
            Font cellFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
            boolean alternate = false;

            for (InvoiceLine line : invoice.getInvoiceLines()) {
                Color bgColor = alternate ? LIGHT_GRAY : Color.WHITE;

                addTableCell(table, line.getPricing().getArticle().getName(), cellFont, bgColor, Element.ALIGN_LEFT);
                addTableCell(table, line.getPricing().getService().getName(), cellFont, bgColor, Element.ALIGN_LEFT);
                addTableCell(table, String.valueOf(line.getQuantity()), cellFont, bgColor, Element.ALIGN_CENTER);
                addTableCell(table, formatMoney(line.getPricing().getPrice()), cellFont, bgColor, Element.ALIGN_RIGHT);
                addTableCell(table, formatMoney(line.getAmount()), cellFont, bgColor, Element.ALIGN_RIGHT);

                alternate = !alternate;
            }

            // Additional fees
            if (invoice.getAdditionalFees() != null && !invoice.getAdditionalFees().isEmpty()) {
                for (AdditionalFees fee : invoice.getAdditionalFees()) {
                    Color bgColor = alternate ? LIGHT_GRAY : Color.WHITE;

                    addTableCell(table, fee.getTitle(), cellFont, bgColor, Element.ALIGN_LEFT);
                    addTableCell(table, fee.getDescription() != null ? fee.getDescription() : "-", cellFont, bgColor, Element.ALIGN_LEFT);
                    addTableCell(table, "1", cellFont, bgColor, Element.ALIGN_CENTER);
                    addTableCell(table, formatMoney(fee.getAmount()), cellFont, bgColor, Element.ALIGN_RIGHT);
                    addTableCell(table, formatMoney(fee.getAmount()), cellFont, bgColor, Element.ALIGN_RIGHT);

                    alternate = !alternate;
                }
            }

            document.add(table);
            document.add(new Paragraph("\n"));

        } catch (DocumentException e) {
            // Skip details if error
        }
    }

    private void addTableCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private void addTotals(Document document, Invoice invoice) {
        try {
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(40);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.setWidths(new float[]{2, 1});

            Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font totalLabelFont = new Font(Font.HELVETICA, 11, Font.BOLD, PRIMARY_COLOR);
            Font totalValueFont = new Font(Font.HELVETICA, 11, Font.BOLD, PRIMARY_COLOR);

            BigDecimal totalHT = invoice.calculateTotalAmount();

            // Sous-total
            addTotalRow(totalsTable, "Sous-total:", formatMoney(totalHT.add(invoice.getDiscount())), labelFont, valueFont);

            // Remise
            if (invoice.getDiscount() != null && invoice.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                addTotalRow(totalsTable, "Remise:", "-" + formatMoney(invoice.getDiscount()), labelFont, valueFont);
            }

            // TVA
            if (invoice.getVatAmount() != null && invoice.getVatAmount().compareTo(BigDecimal.ZERO) > 0) {
                addTotalRow(totalsTable, "TVA:", formatMoney(invoice.getVatAmount()), labelFont, valueFont);
            }

            // Total
            BigDecimal total = totalHT.add(invoice.getVatAmount() != null ? invoice.getVatAmount() : BigDecimal.ZERO);
            addTotalRow(totalsTable, "TOTAL:", formatMoney(total), totalLabelFont, totalValueFont);

            // Montant payé
            addTotalRow(totalsTable, "Montant payé:", formatMoney(invoice.getAmountPaid()), labelFont, valueFont);

            // Reste à payer
            Font restFont = invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0
                    ? new Font(Font.HELVETICA, 11, Font.BOLD, Color.RED)
                    : new Font(Font.HELVETICA, 11, Font.BOLD, new Color(39, 174, 96));
            addTotalRow(totalsTable, "Reste à payer:", formatMoney(invoice.getRemainingAmount()),
                    new Font(Font.HELVETICA, 11, Font.BOLD), restFont);

            document.add(totalsTable);
            document.add(new Paragraph("\n"));

        } catch (DocumentException e) {
            // Skip totals if error
        }
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addFooter(Document document, Company company) {
        try {
            Font footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);

            StringBuilder footer = new StringBuilder();

            if (company.getWebsite() != null) {
                footer.append(company.getWebsite());
            }

            if (company.getSlogan() != null) {
                if (footer.length() > 0) {
                    footer.append(" | ");
                }
                footer.append(company.getSlogan());
            }

            if (footer.length() > 0) {
                Paragraph footerPara = new Paragraph(footer.toString(), footerFont);
                footerPara.setAlignment(Element.ALIGN_CENTER);
                document.add(footerPara);
            }

        } catch (DocumentException e) {
            // Skip footer if error
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0 FCFA";
        return String.format("%,.0f FCFA", amount);
    }
}
