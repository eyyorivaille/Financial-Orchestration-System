package com.financial.project.settlement.internal;

import com.financial.project.settlement.internal.soap.SettlementConfirmationRequest;
import com.financial.project.settlement.internal.soap.SettlementConfirmationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;

@Component
class SoapCoreBankingClient implements CoreBankingClient {

    private final WebServiceTemplate webServiceTemplate;

    SoapCoreBankingClient(
            Jaxb2Marshaller coreBankingMarshaller,
            @Value("${settlement.core-banking.endpoint-uri}") String endpointUri) {
        this.webServiceTemplate = new WebServiceTemplate(coreBankingMarshaller);
        this.webServiceTemplate.setDefaultUri(endpointUri);
    }

    @Override
    @CircuitBreaker(name = "coreBanking", fallbackMethod = "confirmSettlementFallback")
    public SettlementConfirmation confirmSettlement(
            UUID paymentId, String customerId, long amountMinorUnits, String currency) {
        SettlementConfirmationRequest request = new SettlementConfirmationRequest();
        request.setPaymentId(paymentId.toString());
        request.setCustomerId(customerId);
        request.setAmountMinorUnits(amountMinorUnits);
        request.setCurrency(currency);
        try {
            SettlementConfirmationResponse response =
                    (SettlementConfirmationResponse) webServiceTemplate.marshalSendAndReceive(request);
            return SettlementConfirmation.confirmed(response.getReferenceId());
        } catch (RuntimeException e) {
            throw new CoreBankingException("Core banking settlement call failed", e);
        }
    }

    @SuppressWarnings("unused")
    private SettlementConfirmation confirmSettlementFallback(
            UUID paymentId, String customerId, long amountMinorUnits, String currency, Throwable throwable) {
        return SettlementConfirmation.rejected("Core banking unavailable: " + throwable.getMessage());
    }
}
