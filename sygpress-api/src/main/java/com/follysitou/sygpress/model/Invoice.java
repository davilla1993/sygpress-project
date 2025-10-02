package com.follysitou.sygpress.model;

import com.follysitou.sygpress.service.InvoiceNumberGeneratorService;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String invoiceNumber;

    @NotNull(message = "La date de dépôt est obligatoire")
    private LocalDate depositDate;

    @NotNull(message = "La date de livraison est obligatoire")
    @Future(message = "La date de livraison doit être dans le futur")
    private LocalDate deliveryDate;

    @Min(value = 0, message = "La remise ne peut pas être négative")
    private double discount;

    @Min(value = 0, message = "Le montant de TVA ne peut pas être négatif")
    private double vatAmount;

    @Min(value = 0, message = "Le montant payé ne peut pas être négatif")
    private double amountPaid;

    @Min(value = 0, message = "Le montant restant ne peut pas être négatif")
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
