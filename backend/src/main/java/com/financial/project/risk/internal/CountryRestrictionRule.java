package com.financial.project.risk.internal;

import com.financial.project.risk.RiskAssessmentRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class CountryRestrictionRule implements RiskRule {

    private final Set<String> blockedCountries;
    private final int score;

    CountryRestrictionRule(
            @Value("${risk.rules.country.blocked:}") String blockedCountriesCsv,
            @Value("${risk.rules.country.score}") int score) {
        this.blockedCountries = Arrays.stream(blockedCountriesCsv.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(code -> code.toUpperCase(java.util.Locale.ROOT))
                .collect(Collectors.toSet());
        this.score = score;
    }

    @Override
    public RuleOutcome evaluate(RiskAssessmentRequest request) {
        if (blockedCountries.contains(request.countryCode().toUpperCase(java.util.Locale.ROOT))) {
            return new RuleOutcome(score, "country %s is blocked".formatted(request.countryCode()));
        }
        return RuleOutcome.NONE;
    }
}
