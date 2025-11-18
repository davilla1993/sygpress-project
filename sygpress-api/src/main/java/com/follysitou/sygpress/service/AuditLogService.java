package com.follysitou.sygpress.service;

import com.follysitou.sygpress.model.AuditLog;
import com.follysitou.sygpress.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String action, String entityType, String entityId, String details,
                    AuditLog.ActionStatus status, String errorMessage, HttpServletRequest request) {
        String username = getCurrentUsername();
        String ipAddress = request != null ? getClientIp(request) : "N/A";
        String userAgent = request != null ? request.getHeader("User-Agent") : "N/A";

        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .actionDate(LocalDateTime.now())
                .status(status)
                .errorMessage(errorMessage)
                .build();

        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void logSuccess(String action, String entityType, String entityId, String details, HttpServletRequest request) {
        log(action, entityType, entityId, details, AuditLog.ActionStatus.SUCCESS, null, request);
    }

    @Transactional
    public void logFailure(String action, String entityType, String entityId, String details,
                           String errorMessage, HttpServletRequest request) {
        log(action, entityType, entityId, details, AuditLog.ActionStatus.FAILED, errorMessage, request);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "ANONYMOUS";
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
