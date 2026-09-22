package com.financial.project.settlement.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "settlement_record")
@Getter
class SettlementRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "amount_minor_units", nullable = false)
    private long amountMinorUnits;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SettlementStatus status;

    @Column(name = "core_banking_reference")
    private String coreBankingReference;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "settled_at", nullable = false)
    private Instant settledAt;

    protected SettlementRecord() {
        // JPA
    }

    static SettlementRecord settled(
            UUID paymentId, String customerId, long amountMinorUnits, String currency, String coreBankingReference) {
        SettlementRecord record = new SettlementRecord();
        record.paymentId = paymentId;
        record.customerId = customerId;
        record.amountMinorUnits = amountMinorUnits;
        record.currency = currency;
        record.status = SettlementStatus.SETTLED;
        record.coreBankingReference = coreBankingReference;
        record.settledAt = Instant.now();
        return record;
    }

    static SettlementRecord failed(
            UUID paymentId, String customerId, long amountMinorUnits, String currency, String failureReason) {
        SettlementRecord record = new SettlementRecord();
        record.paymentId = paymentId;
        record.customerId = customerId;
        record.amountMinorUnits = amountMinorUnits;
        record.currency = currency;
        record.status = SettlementStatus.FAILED;
        record.failureReason = failureReason;
        record.settledAt = Instant.now();
        return record;
    }
}
