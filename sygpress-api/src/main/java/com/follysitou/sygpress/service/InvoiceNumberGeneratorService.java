package com.follysitou.sygpress.service;

import com.follysitou.sygpress.repository.InvoiceNumberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
public class InvoiceNumberGeneratorService {

    private final InvoiceNumberRepository sequenceRepository;


    @Transactional
    public String getNextInvoiceNumber() {
        final Long sequenceId = 1L;

        sequenceRepository.incrementNextNumber(sequenceId);
        Long nextNumber = sequenceRepository.findLastNumber(sequenceId);

        if (nextNumber == null) {
            nextNumber = 1L;
        }

        // Formater le numéro (Ex: 1 -> "00001")
        DecimalFormat formatter = new DecimalFormat("00000"); // 5 zéros
        return formatter.format(nextNumber);
    }
}
