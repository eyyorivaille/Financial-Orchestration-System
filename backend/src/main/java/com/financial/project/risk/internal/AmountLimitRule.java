package com.financial.project.risk.internal;

import com.financial.project.risk.RiskAssessmentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class AmountLimitRule implements RiskRule {

    private final long reviewThresholdMinorUnits;
    private final long rejectThresholdMinorUnits;
    private final int reviewScore;
    private final int rejectScore;

    AmountLimitRule(
            @Value("${risk.rules.amount.review-threshold-minor-units}") long reviewThresholdMinorUnits,
            @Value("${risk.rules.amount.reject-threshold-minor-units}") long rejectThresholdMinorUnits,
            @Value("${risk.rules.amount.review-score}") int reviewScore,
            @Value("${risk.rules.amount.reject-score}") int rejectScore) {
        this.reviewThresholdMinorUnits = reviewThresholdMinorUnits;
        this.rejectThresholdMinorUnits = rejectThresholdMinorUnits;
        this.reviewScore = reviewScore;
        this.rejectScore = rejectScore;
    }

    @Override
    public RuleOutcome evaluate(RiskAssessmentRequest request) {
        long amount = request.amountMinorUnits();
        if (amount > rejectThresholdMinorUnits) {
            return new RuleOutcome(
                    rejectScore, "amount %d exceeds reject threshold %d".formatted(amount, rejectThresholdMinorUnits));
        }
        if (amount > reviewThresholdMinorUnits) {
            return new RuleOutcome(
                    reviewScore, "amount %d exceeds review threshold %d".formatted(amount, reviewThresholdMinorUnits));
        }
        return RuleOutcome.NONE;
    }
}
