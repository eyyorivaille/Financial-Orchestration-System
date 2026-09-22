package com.financial.project.settlement.internal.soap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CoreBankingSoapEndpointTest {

    private final CoreBankingSoapEndpoint endpoint = new CoreBankingSoapEndpoint();

    @Test
    void confirmsSettlementWithAGeneratedReference() {
        SettlementConfirmationRequest request = new SettlementConfirmationRequest();
        request.setPaymentId("11111111-1111-1111-1111-111111111111");
        request.setCustomerId("customer-1");
        request.setAmountMinorUnits(4_500L);
        request.setCurrency("TRY");

        SettlementConfirmationResponse response = endpoint.confirmSettlement(request);

        assertThat(response.getReferenceId()).isNotBlank();
    }

    @Test
    void chaosAmountSimulatesACoreBankingOutage() {
        SettlementConfirmationRequest request = new SettlementConfirmationRequest();
        request.setPaymentId("11111111-1111-1111-1111-111111111111");
        request.setCustomerId("customer-1");
        request.setAmountMinorUnits(CoreBankingSoapEndpoint.CHAOS_AMOUNT_MINOR_UNITS);
        request.setCurrency("TRY");

        assertThatThrownBy(() -> endpoint.confirmSettlement(request)).isInstanceOf(IllegalStateException.class);
    }
}
