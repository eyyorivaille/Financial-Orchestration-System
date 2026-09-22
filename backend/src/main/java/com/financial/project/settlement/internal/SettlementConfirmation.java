package com.financial.project.settlement.internal;

record SettlementConfirmation(boolean confirmed, String referenceId, String failureReason) {

    static SettlementConfirmation confirmed(String referenceId) {
        return new SettlementConfirmation(true, referenceId, null);
    }

    static SettlementConfirmation rejected(String failureReason) {
        return new SettlementConfirmation(false, null, failureReason);
    }
}
