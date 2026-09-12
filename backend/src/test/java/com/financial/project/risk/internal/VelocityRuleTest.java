package com.financial.project.risk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.financial.project.risk.RiskAssessmentRequest;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VelocityRuleTest {

    @Mock
    private RiskAssessmentRepository repository;

    private VelocityRule rule;

    @BeforeEach
    void setUp() {
        rule = new VelocityRule(repository, 5, 8, 40);
    }

    @Test
    void belowThresholdScoresZero() {
        when(repository.countByCustomerIdAndAssessedAtAfter(anyString(), any(Instant.class)))
                .thenReturn(7L);

        RuleOutcome outcome = rule.evaluate(request());

        assertThat(outcome.score()).isZero();
    }

    @Test
    void atOrAboveThresholdScoresConfiguredPoints() {
        when(repository.countByCustomerIdAndAssessedAtAfter(anyString(), any(Instant.class)))
                .thenReturn(8L);

        RuleOutcome outcome = rule.evaluate(request());

        assertThat(outcome.score()).isEqualTo(40);
        assertThat(outcome.reason()).contains("customer-1");
    }

    private RiskAssessmentRequest request() {
        return new RiskAssessmentRequest("customer-1", 1_000L, "TRY", "TR");
    }
}
