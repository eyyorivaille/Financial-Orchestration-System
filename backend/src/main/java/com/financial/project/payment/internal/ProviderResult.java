package com.financial.project.payment.internal;

public record ProviderResult(boolean successful, String failureReason) {

    public static ProviderResult success() {
        return new ProviderResult(true, null);
    }

    public static ProviderResult failure(String reason) {
        return new ProviderResult(false, reason);
    }
}
