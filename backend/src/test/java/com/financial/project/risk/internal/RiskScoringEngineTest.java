package com.financial.project.risk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financial.project.risk.RiskAssessmentRequest;
import com.financial.project.risk.RiskDecision;
import com.financial.project.risk.RiskOutcome;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RiskScoringEngineTest {

    @Mock
    private RiskRule lowScoreRule;

    @Mock
    private RiskRule highScoreRule;

    @Mock
    private RiskAssessmentRepository repository;

    @Test
    void noTriggeredRulesResultsInApprove() {
        when(lowScoreRule.evaluate(any())).thenReturn(RuleOutcome.NONE);
        when(highScoreRule.evaluate(any())).thenReturn(RuleOutcome.NONE);
        RiskScoringEngine engine = new RiskScoringEngine(List.of(lowScoreRule, highScoreRule), repository, 30, 70);

        RiskDecision decision = engine.assess(request());

        assertThat(decision.outcome()).isEqualTo(RiskOutcome.APPROVE);
        assertThat(decision.score()).isZero();
        assertThat(decision.reason()).isNull();
        verify(repository, times(1)).save(any(RiskAssessment.class));
    }

    @Test
    void combinedScoreBelowRejectButAboveReviewResultsInReview() {
        when(lowScoreRule.evaluate(any())).thenReturn(new RuleOutcome(20, "flag-a"));
        when(highScoreRule.evaluate(any())).thenReturn(new RuleOutcome(15, "flag-b"));
        RiskScoringEngine engine = new RiskScoringEngine(List.of(lowScoreRule, highScoreRule), repository, 30, 70);

        RiskDecision decision = engine.assess(request());

        assertThat(decision.outcome()).isEqualTo(RiskOutcome.REVIEW);
        assertThat(decision.score()).isEqualTo(35);
        assertThat(decision.reason()).contains("flag-a").contains("flag-b");
    }

    @Test
    void combinedScoreAtOrAboveRejectThresholdResultsInReject() {
        when(lowScoreRule.evaluate(any())).thenReturn(new RuleOutcome(40, "flag-a"));
        when(highScoreRule.evaluate(any())).thenReturn(new RuleOutcome(40, "flag-b"));
        RiskScoringEngine engine = new RiskScoringEngine(List.of(lowScoreRule, highScoreRule), repository, 30, 70);

        RiskDecision decision = engine.assess(request());

        assertThat(decision.outcome()).isEqualTo(RiskOutcome.REJECT);
        assertThat(decision.score()).isEqualTo(80);
    }

    private RiskAssessmentRequest request() {
        return new RiskAssessmentRequest("customer-1", 1_000L, "TRY", "TR");
    }
}
