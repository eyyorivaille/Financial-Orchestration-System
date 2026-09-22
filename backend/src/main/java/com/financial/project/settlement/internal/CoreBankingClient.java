package com.financial.project.settlement.internal;

import java.util.UUID;

interface CoreBankingClient {

    SettlementConfirmation confirmSettlement(UUID paymentId, String customerId, long amountMinorUnits, String currency);
}
