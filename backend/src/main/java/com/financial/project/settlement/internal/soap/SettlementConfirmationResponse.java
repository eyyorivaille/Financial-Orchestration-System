package com.financial.project.settlement.internal.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "settlementConfirmationResponse", namespace = SettlementNamespace.URI)
@XmlType(
        name = "",
        namespace = SettlementNamespace.URI,
        propOrder = {"referenceId"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SettlementConfirmationResponse {

    private String referenceId;

    public SettlementConfirmationResponse() {}

    public SettlementConfirmationResponse(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}
