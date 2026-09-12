package com.financial.project.risk;

public record RiskAssessmentRequest(String customerId, long amountMinorUnits, String currency, String countryCode) {}
