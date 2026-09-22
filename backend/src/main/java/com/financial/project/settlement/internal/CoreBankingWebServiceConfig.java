package com.financial.project.settlement.internal;

import com.financial.project.settlement.internal.soap.SettlementConfirmationRequest;
import com.financial.project.settlement.internal.soap.SettlementConfirmationResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
class CoreBankingWebServiceConfig {

    @Bean
    Jaxb2Marshaller coreBankingMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(SettlementConfirmationRequest.class, SettlementConfirmationResponse.class);
        return marshaller;
    }
}
