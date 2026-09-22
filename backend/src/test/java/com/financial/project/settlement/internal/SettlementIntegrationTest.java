package com.financial.project.settlement.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Exercises the full Payment -> Kafka -> Settlement flow. CoreBankingClient is
 * mocked here (same boundary-mocking approach as PaymentServiceTest mocking
 * PaymentProviderClient) so this test doesn't depend on a real SOAP/HTTP round
 * trip - that wire-level behavior is covered separately by
 * SettlementJaxbMarshallingTest, CoreBankingSoapEndpointTest, and the live
 * docker-compose verification.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SettlementIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SettlementRecordRepository settlementRecordRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @MockitoBean
    private CoreBankingClient coreBankingClient;

    @Test
    void completedPaymentIsSettledAndBookedAsADebitCreditPairViaKafka() throws Exception {
        when(coreBankingClient.confirmSettlement(any(), any(), anyLong(), any()))
                .thenReturn(SettlementConfirmation.confirmed("mock-reference"));

        String idempotencyKey = "settlement-it-" + System.nanoTime();

        String response = mockMvc.perform(post("/api/payments")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.subject("settlement-customer-1")))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountMinorUnits\": 4500, \"currency\": \"TRY\", \"countryCode\": \"TR\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID paymentId =
                UUID.fromString(objectMapper.readTree(response).get("id").asText());

        // Payment -> Kafka -> Settlement listener is asynchronous, so poll instead of asserting immediately.
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            List<SettlementRecord> records = settlementRecordRepository.findByPaymentId(paymentId);
            assertThat(records).hasSize(1);
            SettlementRecord record = records.get(0);
            assertThat(record.getStatus()).isEqualTo(SettlementStatus.SETTLED);
            assertThat(record.getCoreBankingReference()).isEqualTo("mock-reference");

            List<LedgerEntry> entries = ledgerEntryRepository.findBySettlementRecordId(record.getId());
            assertThat(entries).hasSize(2);
            long debit = entries.stream()
                    .filter(e -> e.getEntryType() == LedgerEntryType.DEBIT)
                    .mapToLong(LedgerEntry::getAmountMinorUnits)
                    .sum();
            long credit = entries.stream()
                    .filter(e -> e.getEntryType() == LedgerEntryType.CREDIT)
                    .mapToLong(LedgerEntry::getAmountMinorUnits)
                    .sum();
            assertThat(debit).isEqualTo(credit).isEqualTo(4500L);
        });
    }
}
