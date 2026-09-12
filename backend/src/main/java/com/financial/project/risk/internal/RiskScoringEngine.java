package com.financial.project.risk.internal;

import com.financial.project.risk.RiskAssessmentApi;
import com.financial.project.risk.RiskAssessmentRequest;
import com.financial.project.risk.RiskDecision;
import com.financial.project.risk.RiskOutcome;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RiskScoringEngine implements RiskAssessmentApi {

    private final List<RiskRule> rules;
    private final RiskAssessmentRepository repository;
    private final int reviewScoreThreshold;
    private final int rejectScoreThreshold;

    RiskScoringEngine(
            List<RiskRule> rules,
            RiskAssessmentRepository repository,
            @Value("${risk.outcome.review-score-threshold}") int reviewScoreThreshold,
            @Value("${risk.outcome.reject-score-threshold}") int rejectScoreThreshold) {
        this.rules = rules;
        this.repository = repository;
        this.reviewScoreThreshold = reviewScoreThreshold;
        this.rejectScoreThreshold = rejectScoreThreshold;
    }

    @Override
    @Transactional
    public RiskDecision assess(RiskAssessmentRequest request) {
        List<RuleOutcome> triggered = rules.stream()
                .map(rule -> rule.evaluate(request))
                .filter(outcome -> outcome.score() > 0)
                .toList();

        int totalScore = triggered.stream().mapToInt(RuleOutcome::score).sum();
        String reason = triggered.isEmpty()
                ? null
                : triggered.stream().map(RuleOutcome::reason).collect(Collectors.joining("; "));

        RiskOutcome outcome = toOutcome(totalScore);
        repository.save(RiskAssessment.record(request, totalScore, outcome, reason));
        return new RiskDecision(outcome, totalScore, reason);
    }

    private RiskOutcome toOutcome(int score) {
        if (score >= rejectScoreThreshold) {
            return RiskOutcome.REJECT;
        }
        if (score >= reviewScoreThreshold) {
            return RiskOutcome.REVIEW;
        }
        return RiskOutcome.APPROVE;
    }
}
