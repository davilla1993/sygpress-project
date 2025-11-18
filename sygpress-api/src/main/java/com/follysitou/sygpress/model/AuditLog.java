package com.follysitou.sygpress.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_action_date", columnList = "actionDate"),
        @Index(name = "idx_entity_type", columnList = "entityType"),
        @Index(name = "idx_action_status", columnList = "status")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {

    @Column(name = "username", length = 255)
    private String username;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 50)
    private String entityType;

    @Column(length = 100)
    private String entityId;

    @Column(length = 2000)
    private String details;

    @Column(length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime actionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionStatus status;

    @Column(length = 1000)
    private String errorMessage;

    public enum ActionStatus {
        SUCCESS,
        FAILED
    }
}


