package com.financial.project.settlement.internal.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "settlementConfirmationRequest", namespace = SettlementNamespace.URI)
@XmlType(
        name = "",
        namespace = SettlementNamespace.URI,
        propOrder = {"paymentId", "customerId", "amountMinorUnits", "currency"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SettlementConfirmationRequest {

    private String paymentId;
    private String customerId;
    private long amountMinorUnits;
    private String currency;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public long getAmountMinorUnits() {
        return amountMinorUnits;
    }

    public void setAmountMinorUnits(long amountMinorUnits) {
        this.amountMinorUnits = amountMinorUnits;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
