package com.follysitou.sygpress.service;

import com.follysitou.sygpress.model.Invoice;
import com.follysitou.sygpress.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceNumberGeneratorService numberGeneratorService;


    @Transactional
    public Invoice saveNewInvoice(Invoice newInvoice) {
        // Injecter le service avant d'appeler save, ce qui déclenchera @PrePersist
        newInvoice.setNumberGeneratorService(numberGeneratorService);

        // Le @PrePersist dans Invoice est appelé ici, générant le numéro
        return invoiceRepository.save(newInvoice);
    }
}
