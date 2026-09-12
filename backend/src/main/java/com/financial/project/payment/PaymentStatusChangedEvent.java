package com.financial.project.payment;

import java.time.Instant;
import java.util.UUID;
import org.springframework.modulith.events.Externalized;

@Externalized("payment-events::#{paymentId}")
public record PaymentStatusChangedEvent(UUID paymentId, PaymentStatus status, String reason, Instant occurredAt) {}
