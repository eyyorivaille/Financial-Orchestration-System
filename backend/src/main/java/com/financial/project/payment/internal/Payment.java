package com.financial.project.payment.internal;

import com.financial.project.payment.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "payment_transaction")
@Getter
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "amount_minor_units", nullable = false)
    private long amountMinorUnits;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Payment() {
        // JPA
    }

    public static Payment create(String idempotencyKey, long amountMinorUnits, String currency) {
        Payment payment = new Payment();
        payment.idempotencyKey = idempotencyKey;
        payment.amountMinorUnits = amountMinorUnits;
        payment.currency = currency;
        payment.status = PaymentStatus.CREATED;
        Instant now = Instant.now();
        payment.createdAt = now;
        payment.updatedAt = now;
        return payment;
    }

    public void transitionTo(PaymentStatus target) {
        status.assertTransitionAllowed(target);
        this.status = target;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String reason) {
        transitionTo(PaymentStatus.FAILED);
        this.failureReason = reason;
    }
}
