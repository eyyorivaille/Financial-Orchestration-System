package com.financial.project.settlement.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financial.project.payment.PaymentStatus;
import com.financial.project.payment.PaymentStatusChangedEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private CoreBankingClient coreBankingClient;

    @Mock
    private SettlementRecordRepository settlementRecordRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    private SettlementService settlementService;

    @BeforeEach
    void setUp() {
        settlementService = new SettlementService(coreBankingClient, settlementRecordRepository, ledgerEntryRepository);
    }

    @Test
    void confirmedSettlementBooksOneDebitAndOneCreditOfEqualAmount() {
        when(settlementRecordRepository.save(any(SettlementRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(coreBankingClient.confirmSettlement(any(), any(), anyLong(), any()))
                .thenReturn(SettlementConfirmation.confirmed("ref-123"));

        settlementService.handlePaymentStatusChanged(event(PaymentStatus.COMPLETED, 4_500L, "TRY"));

        ArgumentCaptor<SettlementRecord> recordCaptor = ArgumentCaptor.forClass(SettlementRecord.class);
        verify(settlementRecordRepository).save(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getStatus()).isEqualTo(SettlementStatus.SETTLED);
        assertThat(recordCaptor.getValue().getCoreBankingReference()).isEqualTo("ref-123");

        ArgumentCaptor<LedgerEntry> entryCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerEntryRepository, times(2)).save(entryCaptor.capture());
        var entries = entryCaptor.getAllValues();
        assertThat(entries).hasSize(2);
        assertThat(entries.stream()
                        .mapToLong(LedgerEntry::getAmountMinorUnits)
                        .distinct()
                        .count())
                .isEqualTo(1);
        assertThat(entries.stream().map(LedgerEntry::getEntryType))
                .containsExactlyInAnyOrder(LedgerEntryType.DEBIT, LedgerEntryType.CREDIT);
    }

    @Test
    void rejectedConfirmationRecordsFailureWithoutBookingLedgerEntries() {
        when(settlementRecordRepository.save(any(SettlementRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(coreBankingClient.confirmSettlement(any(), any(), anyLong(), any()))
                .thenReturn(SettlementConfirmation.rejected("Core banking unavailable: simulated outage"));

        settlementService.handlePaymentStatusChanged(event(PaymentStatus.COMPLETED, 1_000L, "TRY"));

        ArgumentCaptor<SettlementRecord> recordCaptor = ArgumentCaptor.forClass(SettlementRecord.class);
        verify(settlementRecordRepository).save(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getStatus()).isEqualTo(SettlementStatus.FAILED);
        assertThat(recordCaptor.getValue().getFailureReason()).contains("simulated outage");
        verify(ledgerEntryRepository, never()).save(any());
    }

    @Test
    void nonCompletedStatusIsIgnored() {
        settlementService.handlePaymentStatusChanged(event(PaymentStatus.FAILED, 1_000L, "TRY"));

        verify(coreBankingClient, never()).confirmSettlement(any(), anyString(), anyLong(), anyString());
        verify(settlementRecordRepository, never()).save(any());
        verify(ledgerEntryRepository, never()).save(any());
    }

    private PaymentStatusChangedEvent event(PaymentStatus status, long amountMinorUnits, String currency) {
        return new PaymentStatusChangedEvent(
                UUID.randomUUID(), "customer-1", amountMinorUnits, currency, status, null, Instant.now());
    }
}
