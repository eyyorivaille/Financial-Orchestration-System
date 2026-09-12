package com.financial.project.payment.internal;

/**
 * Temporary in-module boundary standing in for the future Risk module. Once
 * Risk exists, replace {@link AlwaysApproveRiskAssessment} with a call into
 * Risk's public API instead of deleting this interface's call sites.
 */
public interface RiskAssessmentPort {

    RiskDecision assess(long amountMinorUnits, String currency);
}
