package com.financial.project.risk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.financial.project.risk.RiskAssessmentRequest;
import org.junit.jupiter.api.Test;

class CountryRestrictionRuleTest {

    private final CountryRestrictionRule rule = new CountryRestrictionRule("XX, YY", 50);

    @Test
    void allowedCountryScoresZero() {
        RuleOutcome outcome = rule.evaluate(request("TR"));
        assertThat(outcome.score()).isZero();
    }

    @Test
    void blockedCountryScoresConfiguredPoints() {
        RuleOutcome outcome = rule.evaluate(request("XX"));
        assertThat(outcome.score()).isEqualTo(50);
        assertThat(outcome.reason()).contains("XX");
    }

    @Test
    void blockedListIsCaseInsensitive() {
        RuleOutcome outcome = rule.evaluate(request("yy"));
        assertThat(outcome.score()).isEqualTo(50);
    }

    @Test
    void emptyBlockedListNeverTriggers() {
        CountryRestrictionRule noBlocklist = new CountryRestrictionRule("", 50);
        assertThat(noBlocklist.evaluate(request("XX")).score()).isZero();
    }

    private RiskAssessmentRequest request(String countryCode) {
        return new RiskAssessmentRequest("customer-1", 1_000L, "TRY", countryCode);
    }
}
