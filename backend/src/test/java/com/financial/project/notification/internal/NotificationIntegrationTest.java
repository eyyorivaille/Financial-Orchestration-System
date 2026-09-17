package com.financial.project.notification.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.financial.project.AbstractIntegrationTest;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void completedPaymentTriggersNotificationAsynchronouslyViaKafka() throws Exception {
        String idempotencyKey = "notif-it-" + System.nanoTime();

        String response = mockMvc.perform(post("/api/payments")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject("notif-customer-1")))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountMinorUnits\": 2500, \"currency\": \"TRY\", \"countryCode\": \"TR\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID paymentId =
                UUID.fromString(objectMapper.readTree(response).get("id").asText());

        // Payment -> Kafka -> Notification listener is asynchronous, so poll instead of asserting immediately.
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByPaymentId(paymentId);
            assertThat(notifications).hasSize(1);
            assertThat(notifications.get(0).getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(notifications.get(0).getCustomerId()).isEqualTo("notif-customer-1");
            assertThat(notifications.get(0).getMessage()).contains("25.00 TRY");
        });
    }
}
