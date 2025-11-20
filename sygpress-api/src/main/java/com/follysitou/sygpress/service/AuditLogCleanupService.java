package com.follysitou.sygpress.service;

import com.follysitou.sygpress.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        value = "audit.cleanup.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AuditLogCleanupService {

    private final AuditLogRepository auditLogRepository;

    @Value("${audit.cleanup.retention-days:30}")
    private int retentionDays;

    /**
     * Nettoie les logs d'audit de plus de X jours
     * S'exécute le 1er de chaque mois à 2h00 du matin
     */
    @Scheduled(cron = "${audit.cleanup.cron:0 0 2 1 * ?}")
    @Transactional
    public void cleanupOldAuditLogs() {
        log.info("Démarrage du nettoyage des logs d'audit de plus de {} jours", retentionDays);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        try {
            int deletedCount = auditLogRepository.deleteByActionDateBefore(cutoffDate);
            log.info("Nettoyage terminé : {} logs d'audit supprimés (antérieurs au {})",
                    deletedCount, cutoffDate);
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des logs d'audit", e);
        }
    }

    /**
     * Méthode pour nettoyer manuellement les logs (peut être appelée par un endpoint admin)
     */
    @Transactional
    public int cleanupManually(int days) {
        log.info("Nettoyage manuel des logs d'audit de plus de {} jours", days);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        int deletedCount = auditLogRepository.deleteByActionDateBefore(cutoffDate);
        log.info("Nettoyage manuel terminé : {} logs supprimés", deletedCount);
        return deletedCount;
    }
}
