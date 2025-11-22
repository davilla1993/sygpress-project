package com.follysitou.sygpress.service;

import com.follysitou.sygpress.model.Invoice;
import com.follysitou.sygpress.model.Sequence;
import com.follysitou.sygpress.repository.InvoiceNumberRepository;
import com.follysitou.sygpress.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceNumberGeneratorService {

    private final InvoiceNumberRepository sequenceRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public synchronized String getNextInvoiceNumber() {
        final Long sequenceId = 1L;
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                // Utiliser un verrou pessimiste pour éviter les problèmes de concurrence
                Sequence sequence = sequenceRepository.findByIdWithLock(sequenceId)
                        .orElseGet(() -> {
                            // Créer la séquence si elle n'existe pas
                            Sequence newSequence = new Sequence(sequenceId, 0L);
                            return sequenceRepository.save(newSequence);
                        });

                // Incrémenter le numéro
                Long nextNumber = sequence.getLastNumber() + 1;

                // Vérifier si ce numéro existe déjà (sécurité supplémentaire)
                DecimalFormat formatter = new DecimalFormat("00000");
                String formattedNumber = formatter.format(nextNumber);

                Optional<Invoice> existingInvoice = invoiceRepository.findByInvoiceNumber(formattedNumber);
                if (existingInvoice.isPresent()) {
                    log.warn("Le numéro de facture {} existe déjà. Synchronisation de la séquence...", formattedNumber);
                    synchronizeSequence(sequence);
                    attempt++;
                    continue;
                }

                // Sauvegarder la nouvelle valeur de la séquence
                sequence.setLastNumber(nextNumber);
                sequenceRepository.save(sequence);

                log.debug("Nouveau numéro de facture généré: {}", formattedNumber);
                return formattedNumber;

            } catch (DataIntegrityViolationException e) {
                log.error("Conflit de numéro de facture détecté (tentative {}/{})", attempt + 1, maxRetries, e);
                attempt++;

                if (attempt >= maxRetries) {
                    throw new RuntimeException("Impossible de générer un numéro de facture unique après " + maxRetries + " tentatives", e);
                }

                // Resynchroniser la séquence avant de réessayer
                Sequence sequence = sequenceRepository.findByIdWithLock(sequenceId)
                        .orElseThrow(() -> new RuntimeException("Séquence introuvable"));
                synchronizeSequence(sequence);
            }
        }

        throw new RuntimeException("Impossible de générer un numéro de facture unique");
    }

    /**
     * Synchronise la séquence avec le dernier numéro de facture existant dans la base
     */
    private void synchronizeSequence(Sequence sequence) {
        Optional<Invoice> lastInvoice = invoiceRepository.findTopByOrderByInvoiceNumberDesc();

        if (lastInvoice.isPresent()) {
            try {
                String lastInvoiceNumber = lastInvoice.get().getInvoiceNumber();
                Long lastNumber = Long.parseLong(lastInvoiceNumber);

                if (lastNumber > sequence.getLastNumber()) {
                    log.info("Synchronisation: mise à jour de la séquence de {} à {}",
                            sequence.getLastNumber(), lastNumber);
                    sequence.setLastNumber(lastNumber);
                    sequenceRepository.save(sequence);
                }
            } catch (NumberFormatException e) {
                log.error("Impossible de parser le numéro de facture", e);
            }
        }
    }
}
