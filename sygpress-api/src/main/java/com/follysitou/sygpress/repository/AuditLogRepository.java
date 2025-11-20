package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByUsername(String username, Pageable pageable);
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
    Page<AuditLog> findByActionDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<AuditLog> findByUsernameAndActionDateBetween(String username, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.actionDate < :cutoffDate")
    int deleteByActionDateBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
