package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.AdditionalFees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdditionalFeesRepository extends JpaRepository<AdditionalFees, Long> {
    List<AdditionalFees> findByInvoiceId(Long invoiceId);
}
