package com.financial.project.payment.internal;

public record RiskDecision(RiskOutcome outcome, String reason) {

    public static RiskDecision approve() {
        return new RiskDecision(RiskOutcome.APPROVE, null);
    }

    public static RiskDecision reject(String reason) {
        return new RiskDecision(RiskOutcome.REJECT, reason);
    }

    public boolean isApproved() {
        return outcome == RiskOutcome.APPROVE;
    }
}
