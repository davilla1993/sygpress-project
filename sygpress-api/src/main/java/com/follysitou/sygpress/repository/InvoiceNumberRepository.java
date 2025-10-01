package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface InvoiceNumberRepository extends JpaRepository<Sequence, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Sequence s SET s.lastNumber = s.lastNumber + 1 WHERE s.id = :id")
    void incrementNextNumber(Long id);

    @Query("SELECT s.lastNumber FROM Sequence s WHERE s.id = :id")
    Long findLastNumber(Long id);
}
