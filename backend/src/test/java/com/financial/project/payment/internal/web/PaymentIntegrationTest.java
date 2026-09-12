package com.financial.project.payment.internal.web;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.financial.project.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPaymentRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .header("Idempotency-Key", "it-no-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(1_000, "TRY")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPaymentSucceedsAndIsIdempotent() throws Exception {
        String idempotencyKey = "it-key-" + System.nanoTime();

        String firstResponse = mockMvc.perform(post("/api/payments")
                        .with(jwt())
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(15_000, "TRY")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String paymentId = objectMapper.readTree(firstResponse).get("id").asText();

        // Same Idempotency-Key must return the same payment, not create a new one.
        mockMvc.perform(post("/api/payments")
                        .with(jwt())
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(15_000, "TRY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(paymentId)));

        mockMvc.perform(get("/api/payments/{id}", paymentId).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void chaosAmountFailsThroughCircuitBreakerFallback() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .with(jwt())
                        .header("Idempotency-Key", "it-chaos-" + System.nanoTime())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(999_999, "TRY")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("FAILED")));
    }

    @Test
    void invalidAmountIsRejectedWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .with(jwt())
                        .header("Idempotency-Key", "it-invalid-" + System.nanoTime())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(-5, "TRY")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownPaymentReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/payments/{id}", "00000000-0000-0000-0000-000000000000")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    private String requestBody(long amountMinorUnits, String currency) {
        return "{\"amountMinorUnits\": %d, \"currency\": \"%s\"}".formatted(amountMinorUnits, currency);
    }
}
