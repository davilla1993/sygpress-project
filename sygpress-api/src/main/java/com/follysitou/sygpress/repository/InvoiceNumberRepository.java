package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.Sequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface InvoiceNumberRepository extends JpaRepository<Sequence, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Sequence s SET s.lastNumber = s.lastNumber + 1 WHERE s.id = :id")
    void incrementNextNumber(Long id);

    @Query("SELECT s.lastNumber FROM Sequence s WHERE s.id = :id")
    Long findLastNumber(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sequence s WHERE s.id = :id")
    Optional<Sequence> findByIdWithLock(Long id);
}
