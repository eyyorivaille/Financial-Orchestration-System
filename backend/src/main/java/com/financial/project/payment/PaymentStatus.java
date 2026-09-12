package com.financial.project.payment;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum PaymentStatus {
    CREATED,
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED;

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(PaymentStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(CREATED, EnumSet.of(PENDING, FAILED, CANCELLED));
        ALLOWED_TRANSITIONS.put(PENDING, EnumSet.of(PROCESSING, CANCELLED));
        ALLOWED_TRANSITIONS.put(PROCESSING, EnumSet.of(COMPLETED, FAILED));
        ALLOWED_TRANSITIONS.put(COMPLETED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(FAILED, EnumSet.noneOf(PaymentStatus.class));
        ALLOWED_TRANSITIONS.put(CANCELLED, EnumSet.noneOf(PaymentStatus.class));
    }

    public boolean canTransitionTo(PaymentStatus target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }

    public void assertTransitionAllowed(PaymentStatus target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateTransitionException(this, target);
        }
    }
}
