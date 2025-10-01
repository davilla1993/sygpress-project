package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
