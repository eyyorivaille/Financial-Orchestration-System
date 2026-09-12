package com.financial.project.risk.internal;

import com.financial.project.risk.RiskAssessmentRequest;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class VelocityRule implements RiskRule {

    private final RiskAssessmentRepository repository;
    private final Duration window;
    private final long maxCount;
    private final int score;

    VelocityRule(
            RiskAssessmentRepository repository,
            @Value("${risk.rules.velocity.window-minutes}") long windowMinutes,
            @Value("${risk.rules.velocity.max-count}") long maxCount,
            @Value("${risk.rules.velocity.score}") int score) {
        this.repository = repository;
        this.window = Duration.ofMinutes(windowMinutes);
        this.maxCount = maxCount;
        this.score = score;
    }

    @Override
    public RuleOutcome evaluate(RiskAssessmentRequest request) {
        long recentCount = repository.countByCustomerIdAndAssessedAtAfter(
                request.customerId(), Instant.now().minus(window));
        if (recentCount >= maxCount) {
            return new RuleOutcome(
                    score,
                    "customer %s made %d assessments in the last %s"
                            .formatted(request.customerId(), recentCount, window));
        }
        return RuleOutcome.NONE;
    }
}
