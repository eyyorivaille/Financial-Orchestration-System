package com.financial.project.risk.internal;

public record RuleOutcome(int score, String reason) {

    public static final RuleOutcome NONE = new RuleOutcome(0, null);
}
