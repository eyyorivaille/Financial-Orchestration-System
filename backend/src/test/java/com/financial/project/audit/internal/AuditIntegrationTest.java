package com.financial.project.audit.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.financial.project.AbstractIntegrationTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuditIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void everyPaymentEventIsRecordedInTheAuditTrail() throws Exception {
        String customerId = "audit-customer-1";
        String idempotencyKey = "audit-it-" + System.nanoTime();

        mockMvc.perform(post("/api/payments")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(customerId)))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountMinorUnits\": 3300, \"currency\": \"TRY\", \"countryCode\": \"TR\"}"))
                .andExpect(status().isCreated());

        // Payment -> Kafka -> Audit listener is asynchronous, so poll instead of asserting immediately.
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            List<AuditLog> created = auditLogRepository.findByEventTypeAndActor("PaymentCreatedEvent", customerId);
            List<AuditLog> statusChanges =
                    auditLogRepository.findByEventTypeAndActor("PaymentStatusChangedEvent", customerId);

            assertThat(created).hasSize(1);
            assertThat(created.get(0).getPayload()).contains("3300");
            // PENDING, PROCESSING, COMPLETED - one status-changed event per transition.
            assertThat(statusChanges).hasSize(3);
        });
    }
}
