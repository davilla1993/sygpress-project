package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.InvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, Long> {
    List<InvoiceLine> findByInvoiceId(Long invoiceId);
    List<InvoiceLine> findByPricingId(Long pricingId);
}
