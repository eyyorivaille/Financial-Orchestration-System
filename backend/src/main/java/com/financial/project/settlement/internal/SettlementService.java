package com.financial.project.settlement.internal;

import com.financial.project.payment.PaymentStatus;
import com.financial.project.payment.PaymentStatusChangedEvent;
import org.springframework.stereotype.Service;

@Service
class SettlementService {

    private static final String CUSTOMER_CLEARING_ACCOUNT = "CUSTOMER_CLEARING";
    private static final String MERCHANT_SETTLEMENT_ACCOUNT = "MERCHANT_SETTLEMENT";

    private final CoreBankingClient coreBankingClient;
    private final SettlementRecordRepository settlementRecordRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    SettlementService(
            CoreBankingClient coreBankingClient,
            SettlementRecordRepository settlementRecordRepository,
            LedgerEntryRepository ledgerEntryRepository) {
        this.coreBankingClient = coreBankingClient;
        this.settlementRecordRepository = settlementRecordRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    void handlePaymentStatusChanged(PaymentStatusChangedEvent event) {
        // Only successful payments move money and get booked to the ledger.
        if (event.status() != PaymentStatus.COMPLETED) {
            return;
        }

        SettlementConfirmation confirmation = coreBankingClient.confirmSettlement(
                event.paymentId(), event.customerId(), event.amountMinorUnits(), event.currency());

        if (!confirmation.confirmed()) {
            settlementRecordRepository.save(SettlementRecord.failed(
                    event.paymentId(),
                    event.customerId(),
                    event.amountMinorUnits(),
                    event.currency(),
                    confirmation.failureReason()));
            return;
        }

        SettlementRecord settlementRecord = settlementRecordRepository.save(SettlementRecord.settled(
                event.paymentId(),
                event.customerId(),
                event.amountMinorUnits(),
                event.currency(),
                confirmation.referenceId()));

        ledgerEntryRepository.save(LedgerEntry.of(
                settlementRecord.getId(),
                CUSTOMER_CLEARING_ACCOUNT,
                LedgerEntryType.DEBIT,
                event.amountMinorUnits(),
                event.currency()));
        ledgerEntryRepository.save(LedgerEntry.of(
                settlementRecord.getId(),
                MERCHANT_SETTLEMENT_ACCOUNT,
                LedgerEntryType.CREDIT,
                event.amountMinorUnits(),
                event.currency()));
    }
}
