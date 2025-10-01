package com.follysitou.sygpress.model;

import com.follysitou.sygpress.service.InvoiceNumberGeneratorService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String invoiceNumber;
    private LocalDate depositDate;
    private LocalDate deliveryDate;
    private double discount;
    private double vatAmount;
    private double amountPaid;
    private double remainingAmount;
    private boolean invoicePaid;

    @ManyToOne
    private Customer customer;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<InvoiceLine> invoiceLines;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<AdditionalFees> additionalFees;

    @Transient
    private InvoiceNumberGeneratorService numberGeneratorService;

    @PrePersist
    public void generateInvoiceNumber() {
        // S'assurer que le service est disponible et que le numéro n'a pas déjà été défini
        if (this.invoiceNumber == null && numberGeneratorService != null) {
            this.invoiceNumber = numberGeneratorService.getNextInvoiceNumber();
        }
    }

    /*public double calculerMontantTotal(Facture facture) {
        double totalArticles = facture.getLignes().stream()
                .mapToDouble(LigneFacture::getMontant)
                .sum();

        double totalFrais = facture.getFraisSupplements().stream()
                .mapToDouble(FraisSupplementaire::getMontant)
                .sum();

        return totalArticles + totalFrais - facture.getRemise();
    }*/

}
