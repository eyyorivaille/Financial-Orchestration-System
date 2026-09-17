package com.financial.project.audit.internal;

import java.time.Instant;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    AuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    void record(Object event, String eventType, String actor, Instant occurredAt) {
        String payload = objectMapper.writeValueAsString(event);
        auditLogRepository.save(AuditLog.record(eventType, actor, payload, occurredAt));
    }
}
