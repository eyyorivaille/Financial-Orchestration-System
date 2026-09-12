package com.financial.project.payment;

public class IllegalStateTransitionException extends RuntimeException {

    public IllegalStateTransitionException(PaymentStatus from, PaymentStatus to) {
        super("Cannot transition payment from %s to %s".formatted(from, to));
    }
}
