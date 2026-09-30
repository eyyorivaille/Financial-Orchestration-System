package com.financial.project.payment.internal.web;

import com.financial.project.payment.PaymentStatus;
import com.financial.project.payment.internal.Payment;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String customerId,
        long amountMinorUnits,
        String currency,
        PaymentStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getCustomerId(),
                payment.getAmountMinorUnits(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
