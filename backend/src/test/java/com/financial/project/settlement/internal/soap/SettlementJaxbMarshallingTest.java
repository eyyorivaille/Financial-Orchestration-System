package com.financial.project.settlement.internal.soap;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * Proves the JAXB request/response classes actually marshal/unmarshal to real XML
 * matching settlement.xsd, without needing a running HTTP server.
 */
class SettlementJaxbMarshallingTest {

    private final Jaxb2Marshaller marshaller = marshaller();

    @Test
    void marshalsAndUnmarshalsRequestRoundTrip() {
        SettlementConfirmationRequest request = new SettlementConfirmationRequest();
        request.setPaymentId("11111111-1111-1111-1111-111111111111");
        request.setCustomerId("customer-1");
        request.setAmountMinorUnits(4_500L);
        request.setCurrency("TRY");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(request, new StreamResult(out));
        String xml = out.toString();

        assertThat(xml)
                .contains("settlementConfirmationRequest")
                .contains("customer-1")
                .contains("4500");

        Object unmarshalled = marshaller.unmarshal(new StreamSource(new ByteArrayInputStream(out.toByteArray())));
        assertThat(unmarshalled).isInstanceOf(SettlementConfirmationRequest.class);
        SettlementConfirmationRequest result = (SettlementConfirmationRequest) unmarshalled;
        assertThat(result.getPaymentId()).isEqualTo(request.getPaymentId());
        assertThat(result.getCustomerId()).isEqualTo("customer-1");
        assertThat(result.getAmountMinorUnits()).isEqualTo(4_500L);
        assertThat(result.getCurrency()).isEqualTo("TRY");
    }

    @Test
    void marshalsAndUnmarshalsResponseRoundTrip() {
        SettlementConfirmationResponse response = new SettlementConfirmationResponse("ref-123");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(response, new StreamResult(out));

        Object unmarshalled = marshaller.unmarshal(new StreamSource(new ByteArrayInputStream(out.toByteArray())));
        assertThat(unmarshalled).isInstanceOf(SettlementConfirmationResponse.class);
        assertThat(((SettlementConfirmationResponse) unmarshalled).getReferenceId())
                .isEqualTo("ref-123");
    }

    private static Jaxb2Marshaller marshaller() {
        try {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setClassesToBeBound(SettlementConfirmationRequest.class, SettlementConfirmationResponse.class);
            marshaller.afterPropertiesSet();
            return marshaller;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
