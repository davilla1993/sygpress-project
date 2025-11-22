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

    // Format ticket de caisse : 80mm de large (environ 226 points), hauteur variable
    private static final Rectangle TICKET_SIZE = new Rectangle(226, 842); // 80mm x 297mm max

    public byte[] generateInvoicePdf(String invoicePublicId) throws IOException {
        Invoice invoice = invoiceService.findByPublicId(invoicePublicId);
        Optional<Company> companyOpt = companyService.getCompany();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Ticket de caisse avec marges réduites (10 points = ~3.5mm)
        Document document = new Document(TICKET_SIZE, 10, 10, 10, 10);

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
            Font companyNameFont = new Font(Font.HELVETICA, 10, Font.BOLD, PRIMARY_COLOR);
            Font infoFont = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.DARK_GRAY);

            // Nom de l'entreprise centré
            Paragraph companyName = new Paragraph(company.getName(), companyNameFont);
            companyName.setAlignment(Element.ALIGN_CENTER);
            document.add(companyName);

            // Logo (optionnel, réduit)
            if (company.getLogoPath() != null && !company.getLogoPath().isEmpty()) {
                try {
                    Resource logoResource = fileStorageService.loadFileAsResource(company.getLogoPath());
                    byte[] logoBytes = logoResource.getInputStream().readAllBytes();
                    Image logo = Image.getInstance(logoBytes);
                    logo.scaleToFit(60, 50); // Taille réduite pour ticket
                    logo.setAlignment(Element.ALIGN_CENTER);
                    document.add(logo);
                } catch (Exception e) {
                    // Logo loading failed, skip it
                }
            }

            // Infos entreprise centrées
            if (company.getAddress() != null) {
                Paragraph address = new Paragraph(company.getAddress(), infoFont);
                address.setAlignment(Element.ALIGN_CENTER);
                document.add(address);
            }

            if (company.getCity() != null) {
                Paragraph city = new Paragraph(company.getCity(), infoFont);
                city.setAlignment(Element.ALIGN_CENTER);
                document.add(city);
            }

            if (company.getPhoneNumber() != null) {
                Paragraph phone = new Paragraph("Tel: " + company.getPhoneNumber(), infoFont);
                phone.setAlignment(Element.ALIGN_CENTER);
                document.add(phone);
            }

            if (company.getEmail() != null) {
                Paragraph email = new Paragraph(company.getEmail(), infoFont);
                email.setAlignment(Element.ALIGN_CENTER);
                document.add(email);
            }

            // Ligne de séparation
            document.add(new Paragraph("========================================", infoFont));
            document.add(new Paragraph(" "));

        } catch (Exception e) {
            // Skip header if error
        }
    }

    private void addInvoiceTitle(Document document, Invoice invoice) {
        try {
            Font titleFont = new Font(Font.HELVETICA, 9, Font.BOLD, SECONDARY_COLOR);
            Font infoFont = new Font(Font.HELVETICA, 7, Font.NORMAL);

            // Numéro de facture
            Paragraph title = new Paragraph("FACTURE N° " + invoice.getInvoiceNumber(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Dates (format compact pour ticket)
            Paragraph depositDate = new Paragraph("Depot: " + invoice.getDepositDate().format(DATE_FORMATTER), infoFont);
            depositDate.setAlignment(Element.ALIGN_CENTER);
            document.add(depositDate);

            Paragraph deliveryDate = new Paragraph("Livraison: " + invoice.getDeliveryDate().format(DATE_FORMATTER), infoFont);
            deliveryDate.setAlignment(Element.ALIGN_CENTER);
            document.add(deliveryDate);

            document.add(new Paragraph("----------------------------------------", infoFont));
            document.add(new Paragraph(" "));

        } catch (DocumentException e) {
            // Skip title if error
        }
    }

    private void addCustomerInfo(Document document, Invoice invoice) {
        try {
            Customer customer = invoice.getCustomer();
            if (customer == null) return;

            Font labelFont = new Font(Font.HELVETICA, 7, Font.BOLD, PRIMARY_COLOR);
            Font valueFont = new Font(Font.HELVETICA, 7, Font.NORMAL);

            // Informations client centrées et compactes
            Paragraph clientLabel = new Paragraph("CLIENT", labelFont);
            clientLabel.setAlignment(Element.ALIGN_CENTER);
            document.add(clientLabel);

            Paragraph clientName = new Paragraph(customer.getName(), valueFont);
            clientName.setAlignment(Element.ALIGN_CENTER);
            document.add(clientName);

            if (customer.getPhoneNumber() != null) {
                Paragraph phone = new Paragraph("Tel: " + customer.getPhoneNumber(), valueFont);
                phone.setAlignment(Element.ALIGN_CENTER);
                document.add(phone);
            }

            if (customer.getAddress() != null) {
                Paragraph address = new Paragraph(customer.getAddress(), valueFont);
                address.setAlignment(Element.ALIGN_CENTER);
                document.add(address);
            }

            document.add(new Paragraph("----------------------------------------", valueFont));
            document.add(new Paragraph(" "));

        } catch (DocumentException e) {
            // Skip customer info if error
        }
    }

    private void addInvoiceDetails(Document document, Invoice invoice) {
        try {
            // Format ticket simplifié : 3 colonnes (Description, Qté, Montant)
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 2});

            // Header avec police réduite pour ticket
            Font headerFont = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);
            String[] headers = {"Description", "Qte", "Montant"};

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(PRIMARY_COLOR);
                cell.setPadding(4);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Invoice lines avec police réduite
            Font cellFont = new Font(Font.HELVETICA, 6, Font.NORMAL);

            for (InvoiceLine line : invoice.getInvoiceLines()) {
                // Description = Article + Service
                String description = line.getPricing().getArticle().getName() + " - " +
                                   line.getPricing().getService().getName();
                addTableCell(table, description, cellFont, Color.WHITE, Element.ALIGN_LEFT);
                addTableCell(table, String.valueOf(line.getQuantity()), cellFont, Color.WHITE, Element.ALIGN_CENTER);
                addTableCell(table, formatMoney(line.getAmount()), cellFont, Color.WHITE, Element.ALIGN_RIGHT);
            }

            // Additional fees
            if (invoice.getAdditionalFees() != null && !invoice.getAdditionalFees().isEmpty()) {
                for (AdditionalFees fee : invoice.getAdditionalFees()) {
                    String description = fee.getTitle();
                    if (fee.getDescription() != null && !fee.getDescription().isEmpty()) {
                        description += " (" + fee.getDescription() + ")";
                    }
                    addTableCell(table, description, cellFont, Color.WHITE, Element.ALIGN_LEFT);
                    addTableCell(table, "1", cellFont, Color.WHITE, Element.ALIGN_CENTER);
                    addTableCell(table, formatMoney(fee.getAmount()), cellFont, Color.WHITE, Element.ALIGN_RIGHT);
                }
            }

            document.add(table);
            document.add(new Paragraph(" "));

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
            // Format ticket : tableau pleine largeur avec police réduite
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(100);
            totalsTable.setWidths(new float[]{1, 1});

            Font labelFont = new Font(Font.HELVETICA, 6, Font.NORMAL);
            Font valueFont = new Font(Font.HELVETICA, 6, Font.NORMAL);
            Font totalLabelFont = new Font(Font.HELVETICA, 7, Font.BOLD, PRIMARY_COLOR);
            Font totalValueFont = new Font(Font.HELVETICA, 7, Font.BOLD, PRIMARY_COLOR);

            // Ligne de séparation
            document.add(new Paragraph("========================================", labelFont));

            // Montant HT (Hors Taxes, après remise)
            BigDecimal subtotal = invoice.calculateSubtotalAmount();

            // Sous-total (avant remise) si remise existe
            if (invoice.getDiscount() != null && invoice.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal subtotalBeforeDiscount = subtotal.add(invoice.getDiscount());
                addTotalRow(totalsTable, "Sous-total:", formatMoney(subtotalBeforeDiscount), labelFont, valueFont);
                addTotalRow(totalsTable, "Remise:", "-" + formatMoney(invoice.getDiscount()), labelFont, valueFont);
            }

            // Montant HT (après remise)
            addTotalRow(totalsTable, "Montant HT:", formatMoney(subtotal), labelFont, valueFont);

            // TVA
            BigDecimal vatAmount = invoice.calculateVatAmount();
            if (vatAmount != null && vatAmount.compareTo(BigDecimal.ZERO) > 0) {
                String vatLabel = "TVA";
                if (invoice.getVatRate() != null && invoice.getVatRate().compareTo(BigDecimal.ZERO) > 0) {
                    vatLabel = String.format("TVA (%.0f%%):", invoice.getVatRate());
                } else {
                    vatLabel = "TVA:";
                }
                addTotalRow(totalsTable, vatLabel, formatMoney(vatAmount), labelFont, valueFont);
            }

            // Total TTC
            BigDecimal totalTTC = invoice.calculateTotalAmount();
            addTotalRow(totalsTable, "TOTAL TTC:", formatMoney(totalTTC), totalLabelFont, totalValueFont);

            // Montant payé
            addTotalRow(totalsTable, "Montant paye:", formatMoney(invoice.getAmountPaid()), labelFont, valueFont);

            // Reste à payer
            Font restFont = invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0
                    ? new Font(Font.HELVETICA, 7, Font.BOLD, Color.RED)
                    : new Font(Font.HELVETICA, 7, Font.BOLD, new Color(39, 174, 96));
            addTotalRow(totalsTable, "Reste a payer:", formatMoney(invoice.getRemainingAmount()),
                    new Font(Font.HELVETICA, 7, Font.BOLD), restFont);

            document.add(totalsTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("========================================", labelFont));

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
            Font footerFont = new Font(Font.HELVETICA, 6, Font.NORMAL, Color.GRAY);

            // Message de remerciement
            Paragraph thanks = new Paragraph("Merci de votre confiance !", footerFont);
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            if (company.getWebsite() != null) {
                Paragraph website = new Paragraph(company.getWebsite(), footerFont);
                website.setAlignment(Element.ALIGN_CENTER);
                document.add(website);
            }

            if (company.getSlogan() != null) {
                Paragraph slogan = new Paragraph(company.getSlogan(), footerFont);
                slogan.setAlignment(Element.ALIGN_CENTER);
                document.add(slogan);
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
