package com.financial.project.payment;

import java.time.Instant;
import java.util.UUID;
import org.springframework.modulith.events.Externalized;

@Externalized("payment-events::#{paymentId}")
public record PaymentCreatedEvent(UUID paymentId, long amountMinorUnits, String currency, Instant occurredAt) {}
