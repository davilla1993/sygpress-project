package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.enums.ProcessingStatus;
import com.follysitou.sygpress.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByPublicId(String publicId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    // Report queries
    List<Invoice> findByDepositDateBetweenAndDeletedFalse(LocalDate startDate, LocalDate endDate);

    List<Invoice> findByDepositDateAndDeletedFalse(LocalDate date);

    @Query("SELECT i FROM Invoice i WHERE i.depositDate BETWEEN :startDate AND :endDate AND i.deleted = false AND i.invoicePaid = false")
    List<Invoice> findUnpaidInvoicesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Invoice i WHERE i.depositDate BETWEEN :startDate AND :endDate AND i.deleted = false AND i.processingStatus = :status")
    List<Invoice> findByDateRangeAndStatus(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("status") ProcessingStatus status);

    @Query("SELECT COUNT(DISTINCT i.customer.id) FROM Invoice i WHERE i.depositDate BETWEEN :startDate AND :endDate AND i.deleted = false")
    int countDistinctCustomersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT c.id) FROM Invoice i JOIN i.customer c WHERE i.depositDate BETWEEN :startDate AND :endDate AND i.deleted = false AND c.createdAt >= :startDateTime")
    int countNewCustomersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("startDateTime") java.time.LocalDateTime startDateTime);

    // Dashboard queries
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.deleted = false")
    int countAllInvoices();

    @Query("SELECT COALESCE(SUM(i.amountPaid), 0) FROM Invoice i WHERE i.deleted = false")
    java.math.BigDecimal sumAllPaidAmount();

    @Query("SELECT i FROM Invoice i WHERE i.deliveryDate = :date AND i.deleted = false ORDER BY i.createdAt DESC")
    List<Invoice> findByDeliveryDateAndDeletedFalse(@Param("date") LocalDate date);

    @Query("SELECT i FROM Invoice i WHERE i.deleted = false AND i.invoicePaid = false ORDER BY i.depositDate ASC")
    List<Invoice> findAllUnpaidInvoices();

    @Query("SELECT i FROM Invoice i WHERE i.processingStatus = :status AND i.deleted = false")
    List<Invoice> findByProcessingStatusAndDeletedFalse(@Param("status") ProcessingStatus status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.processingStatus = :status AND i.deleted = false")
    int countByProcessingStatusAndDeletedFalse(@Param("status") ProcessingStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.deleted = false ORDER BY i.createdAt DESC")
    List<Invoice> findAllOrderByCreatedAtDesc();
}
