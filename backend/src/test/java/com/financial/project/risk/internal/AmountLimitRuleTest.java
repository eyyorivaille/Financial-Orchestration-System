package com.financial.project.risk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.financial.project.risk.RiskAssessmentRequest;
import org.junit.jupiter.api.Test;

class AmountLimitRuleTest {

    private final AmountLimitRule rule = new AmountLimitRule(1_000_000L, 10_000_000L, 30, 80);

    @Test
    void amountBelowReviewThresholdScoresZero() {
        RuleOutcome outcome = rule.evaluate(request(999_999L));
        assertThat(outcome.score()).isZero();
    }

    @Test
    void amountAboveReviewThresholdScoresReviewPoints() {
        RuleOutcome outcome = rule.evaluate(request(5_000_000L));
        assertThat(outcome.score()).isEqualTo(30);
        assertThat(outcome.reason()).contains("review threshold");
    }

    @Test
    void amountAboveRejectThresholdScoresRejectPoints() {
        RuleOutcome outcome = rule.evaluate(request(20_000_000L));
        assertThat(outcome.score()).isEqualTo(80);
        assertThat(outcome.reason()).contains("reject threshold");
    }

    private RiskAssessmentRequest request(long amountMinorUnits) {
        return new RiskAssessmentRequest("customer-1", amountMinorUnits, "TRY", "TR");
    }
}
