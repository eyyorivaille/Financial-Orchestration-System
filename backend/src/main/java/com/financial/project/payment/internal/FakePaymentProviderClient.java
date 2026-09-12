package com.financial.project.payment.internal;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

@Component
class FakePaymentProviderClient implements PaymentProviderClient {

    /**
     * A deterministic "chaos" amount used to exercise the failure / circuit-breaker
     * path in tests and demos without relying on randomness.
     */
    static final long CHAOS_AMOUNT_MINOR_UNITS = 999_999L;

    @Override
    @CircuitBreaker(name = "paymentProvider", fallbackMethod = "chargeFallback")
    public ProviderResult charge(long amountMinorUnits, String currency) {
        if (amountMinorUnits == CHAOS_AMOUNT_MINOR_UNITS) {
            throw new PaymentProviderException("Simulated provider outage for amount " + amountMinorUnits);
        }
        return ProviderResult.success();
    }

    @SuppressWarnings("unused")
    private ProviderResult chargeFallback(long amountMinorUnits, String currency, Throwable throwable) {
        return ProviderResult.failure("Provider unavailable: " + throwable.getMessage());
    }
}
