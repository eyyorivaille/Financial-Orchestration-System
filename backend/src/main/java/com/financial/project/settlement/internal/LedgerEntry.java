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

/**
 * One leg of a double-entry booking against {@link SettlementRecord}. Every
 * settlement produces exactly one DEBIT and one CREDIT entry of equal amount,
 * so sum(debit) == sum(credit) holds per settlement.
 */
@Entity
@Table(name = "ledger_entry")
@Getter
class LedgerEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "settlement_record_id", nullable = false)
    private UUID settlementRecordId;

    @Column(name = "account", nullable = false)
    private String account;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LedgerEntryType entryType;

    @Column(name = "amount_minor_units", nullable = false)
    private long amountMinorUnits;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected LedgerEntry() {
        // JPA
    }

    static LedgerEntry of(
            UUID settlementRecordId,
            String account,
            LedgerEntryType entryType,
            long amountMinorUnits,
            String currency) {
        LedgerEntry entry = new LedgerEntry();
        entry.settlementRecordId = settlementRecordId;
        entry.account = account;
        entry.entryType = entryType;
        entry.amountMinorUnits = amountMinorUnits;
        entry.currency = currency;
        entry.recordedAt = Instant.now();
        return entry;
    }
}
