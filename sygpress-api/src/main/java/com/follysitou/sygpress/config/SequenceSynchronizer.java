package com.follysitou.sygpress.config;

import com.follysitou.sygpress.model.Invoice;
import com.follysitou.sygpress.model.Sequence;
import com.follysitou.sygpress.repository.InvoiceNumberRepository;
import com.follysitou.sygpress.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Synchronise la séquence de numéros de factures au démarrage de l'application.
 * Cela garantit que la séquence est toujours synchronisée avec le dernier numéro de facture existant.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SequenceSynchronizer implements ApplicationRunner {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceNumberRepository sequenceRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Démarrage de la synchronisation de la séquence de factures...");

        try {
            final Long sequenceId = 1L;

            // Récupérer ou créer la séquence
            Sequence sequence = sequenceRepository.findById(sequenceId)
                    .orElseGet(() -> {
                        log.info("Création de la séquence de factures avec ID = 1");
                        Sequence newSequence = new Sequence(sequenceId, 0L);
                        return sequenceRepository.save(newSequence);
                    });

            Long currentSequenceValue = sequence.getLastNumber();
            log.info("Valeur actuelle de la séquence: {}", currentSequenceValue);

            // Trouver le dernier numéro de facture dans la base
            Optional<Invoice> lastInvoice = invoiceRepository.findTopByOrderByInvoiceNumberDesc();

            if (lastInvoice.isPresent()) {
                String lastInvoiceNumber = lastInvoice.get().getInvoiceNumber();
                log.info("Dernier numéro de facture trouvé: {}", lastInvoiceNumber);

                try {
                    // Convertir le numéro de facture en Long (ex: "00005" -> 5)
                    Long lastInvoiceNumberValue = Long.parseLong(lastInvoiceNumber);

                    // Si le dernier numéro de facture est supérieur à la séquence, synchroniser
                    if (lastInvoiceNumberValue > currentSequenceValue) {
                        log.warn("Désynchronisation détectée! Séquence: {}, Dernier numéro: {}",
                                currentSequenceValue, lastInvoiceNumberValue);
                        sequence.setLastNumber(lastInvoiceNumberValue);
                        sequenceRepository.save(sequence);
                        log.info("Séquence synchronisée avec succès. Nouvelle valeur: {}", lastInvoiceNumberValue);
                    } else {
                        log.info("Séquence déjà synchronisée. Aucune action nécessaire.");
                    }
                } catch (NumberFormatException e) {
                    log.error("Impossible de parser le numéro de facture: {}", lastInvoiceNumber, e);
                }
            } else {
                log.info("Aucune facture trouvée dans la base. La séquence reste à {}", currentSequenceValue);
            }

            log.info("Synchronisation de la séquence terminée avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation de la séquence", e);
        }
    }
}
