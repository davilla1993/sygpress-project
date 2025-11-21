package com.follysitou.sygpress.service;

import com.follysitou.sygpress.model.Sequence;
import com.follysitou.sygpress.repository.InvoiceNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
public class InvoiceNumberGeneratorService {

    private final InvoiceNumberRepository sequenceRepository;

    @Transactional
    public synchronized String getNextInvoiceNumber() {
        final Long sequenceId = 1L;

        // Utiliser un verrou pessimiste pour éviter les problèmes de concurrence
        Sequence sequence = sequenceRepository.findByIdWithLock(sequenceId)
                .orElseGet(() -> {
                    // Créer la séquence si elle n'existe pas
                    Sequence newSequence = new Sequence(sequenceId, 0L);
                    return sequenceRepository.save(newSequence);
                });

        // Incrémenter le numéro
        Long nextNumber = sequence.getLastNumber() + 1;
        sequence.setLastNumber(nextNumber);
        sequenceRepository.save(sequence);

        // Formater le numéro (Ex: 1 -> "00001")
        DecimalFormat formatter = new DecimalFormat("00000"); // 5 zéros
        return formatter.format(nextNumber);
    }
}
