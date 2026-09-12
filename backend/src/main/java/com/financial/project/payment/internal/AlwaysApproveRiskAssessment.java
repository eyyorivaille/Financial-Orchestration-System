package com.financial.project.payment.internal;

import org.springframework.stereotype.Service;

@Service
class AlwaysApproveRiskAssessment implements RiskAssessmentPort {

    @Override
    public RiskDecision assess(long amountMinorUnits, String currency) {
        return RiskDecision.approve();
    }
}
