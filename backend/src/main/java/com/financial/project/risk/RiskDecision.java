package com.financial.project.risk;

public record RiskDecision(RiskOutcome outcome, int score, String reason) {}
