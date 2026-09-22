package com.financial.project.user.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.financial.project.AbstractIntegrationTest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    void syncsProfileFromJwtClaimsOnAnAuthenticatedRequest() throws Exception {
        String customerId = "user-it-customer-" + System.nanoTime();
        String idempotencyKey = "user-it-" + System.nanoTime();

        mockMvc.perform(post("/api/payments")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(customerId)
                                .claim("email", "sync-test@example.com")
                                .claim("given_name", "Sync")
                                .claim("family_name", "Test")))
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amountMinorUnits\": 1000, \"currency\": \"TRY\", \"countryCode\": \"TR\"}"))
                .andExpect(status().isCreated());

        // The sync happens synchronously within the request itself - no Kafka/await needed.
        Optional<UserProfile> profile = userProfileRepository.findById(customerId);
        assertThat(profile).isPresent();
        assertThat(profile.get().getEmail()).isEqualTo("sync-test@example.com");
        assertThat(profile.get().getDisplayName()).isEqualTo("Sync Test");
    }
}
