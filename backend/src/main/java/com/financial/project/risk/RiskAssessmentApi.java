package com.financial.project.risk;

public interface RiskAssessmentApi {

    RiskDecision assess(RiskAssessmentRequest request);
}
