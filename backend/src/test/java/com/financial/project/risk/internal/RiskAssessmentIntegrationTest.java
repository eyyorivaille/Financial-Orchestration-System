package com.financial.project.risk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.financial.project.AbstractIntegrationTest;
import com.financial.project.risk.RiskAssessmentApi;
import com.financial.project.risk.RiskAssessmentRequest;
import com.financial.project.risk.RiskDecision;
import com.financial.project.risk.RiskOutcome;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "risk.rules.country.blocked=XX")
class RiskAssessmentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RiskAssessmentApi riskAssessmentApi;

    @Autowired
    private RiskAssessmentRepository repository;

    @Test
    void normalAmountIsApprovedAndPersisted() {
        RiskDecision decision =
                riskAssessmentApi.assess(new RiskAssessmentRequest("normal-customer", 1_000L, "TRY", "TR"));

        assertThat(decision.outcome()).isEqualTo(RiskOutcome.APPROVE);
        assertThat(repository.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void blockedCountryIsNotApproved() {
        RiskDecision decision =
                riskAssessmentApi.assess(new RiskAssessmentRequest("blocked-country-customer", 1_000L, "TRY", "XX"));

        assertThat(decision.outcome()).isNotEqualTo(RiskOutcome.APPROVE);
        assertThat(decision.reason()).contains("XX");
    }

    @Test
    void repeatedCallsForTheSameCustomerTripTheVelocityRule() {
        String customerId = "velocity-customer";
        RiskDecision lastDecision = null;
        for (int i = 0; i < 10; i++) {
            lastDecision = riskAssessmentApi.assess(new RiskAssessmentRequest(customerId, 1_000L, "TRY", "TR"));
        }

        assertThat(lastDecision).isNotNull();
        assertThat(lastDecision.outcome()).isNotEqualTo(RiskOutcome.APPROVE);
    }
}
