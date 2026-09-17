package com.financial.project.audit.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService(auditLogRepository, new JsonMapper());
    }

    @Test
    void recordsEventAsJsonPayloadWithActorAndOriginalTimestamp() {
        Instant occurredAt = Instant.parse("2026-01-01T10:00:00Z");
        TestEvent event = new TestEvent("customer-1", 12345L);

        auditLogService.record(event, "TestEvent", "customer-1", occurredAt);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getEventType()).isEqualTo("TestEvent");
        assertThat(saved.getActor()).isEqualTo("customer-1");
        assertThat(saved.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(saved.getPayload()).contains("\"customerId\":\"customer-1\"").contains("\"amount\":12345");
    }

    private record TestEvent(String customerId, long amount) {}
}
