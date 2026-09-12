package com.financial.project.payment.internal;

public interface PaymentProviderClient {

    ProviderResult charge(long amountMinorUnits, String currency);
}
