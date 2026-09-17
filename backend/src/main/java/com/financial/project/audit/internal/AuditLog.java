package com.financial.project.audit.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * Append-only audit trail (database plan decision 4: "JSON payload + timestamp
 * + actor", same MySQL instance, no separate NoSQL store). Nothing ever
 * updates or deletes a row - there is deliberately no setter/mutator beyond
 * the constructor, and no repository method other than save/find.
 */
@Entity
@Table(name = "audit_log")
@Getter
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "actor", nullable = false)
    private String actor;

    // columnDefinition is explicit because @Lob alone maps to MySQL tinytext
    // (255 bytes) here - far too small for a serialized event's JSON.
    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected AuditLog() {
        // JPA
    }

    public static AuditLog record(String eventType, String actor, String payload, Instant occurredAt) {
        AuditLog auditLog = new AuditLog();
        auditLog.eventType = eventType;
        auditLog.actor = actor;
        auditLog.payload = payload;
        auditLog.occurredAt = occurredAt;
        auditLog.recordedAt = Instant.now();
        return auditLog;
    }
}
