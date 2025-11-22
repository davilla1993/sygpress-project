package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.invoice.publicId = :invoicePublicId ORDER BY p.paymentDate DESC")
    List<Payment> findByInvoicePublicIdOrderByPaymentDateDesc(@Param("invoicePublicId") String invoicePublicId);
}
