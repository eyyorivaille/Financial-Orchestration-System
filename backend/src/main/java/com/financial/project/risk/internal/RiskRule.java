package com.financial.project.risk.internal;

import com.financial.project.risk.RiskAssessmentRequest;

public interface RiskRule {

    RuleOutcome evaluate(RiskAssessmentRequest request);
}
