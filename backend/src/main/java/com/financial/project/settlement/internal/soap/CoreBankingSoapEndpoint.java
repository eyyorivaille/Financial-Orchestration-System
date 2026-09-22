package com.financial.project.settlement.internal.soap;

import java.util.UUID;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Fake "legacy core banking" system: the counterpart Settlement calls over SOAP to
 * confirm a fund settlement. Deployed in the same app for demo purposes (both the
 * client and this endpoint are written per the backend architecture decision), but
 * reached over a real HTTP/SOAP round trip via shared.WebServiceConfig - not an
 * in-process shortcut.
 */
@Endpoint
class CoreBankingSoapEndpoint {

    /**
     * A deterministic "chaos" amount used to exercise the failure / circuit-breaker
     * path, distinct from FakePaymentProviderClient's own chaos amount so a payment
     * can complete successfully and still fail settlement.
     */
    static final long CHAOS_AMOUNT_MINOR_UNITS = 888_888L;

    @PayloadRoot(namespace = SettlementNamespace.URI, localPart = "settlementConfirmationRequest")
    @ResponsePayload
    SettlementConfirmationResponse confirmSettlement(@RequestPayload SettlementConfirmationRequest request) {
        if (request.getAmountMinorUnits() == CHAOS_AMOUNT_MINOR_UNITS) {
            throw new IllegalStateException(
                    "Simulated core banking outage for amount " + request.getAmountMinorUnits());
        }
        return new SettlementConfirmationResponse(UUID.randomUUID().toString());
    }
}
