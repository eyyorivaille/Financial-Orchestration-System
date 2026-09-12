package com.financial.project.payment.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financial.project.payment.PaymentCreatedEvent;
import com.financial.project.payment.PaymentStatus;
import com.financial.project.payment.PaymentStatusChangedEvent;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RiskAssessmentPort riskAssessmentPort;

    @Mock
    private PaymentProviderClient paymentProviderClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService =
                new PaymentService(paymentRepository, riskAssessmentPort, paymentProviderClient, eventPublisher);
    }

    @Test
    void approvedPaymentGoesThroughToCompleted() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(riskAssessmentPort.assess(10_000L, "TRY")).thenReturn(RiskDecision.approve());
        when(paymentProviderClient.charge(10_000L, "TRY")).thenReturn(ProviderResult.success());

        PaymentCreationOutcome outcome = paymentService.createPayment("key-1", 10_000L, "TRY");

        assertThat(outcome.newlyCreated()).isTrue();
        assertThat(outcome.payment().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(eventPublisher, times(1)).publishEvent(any(PaymentCreatedEvent.class));
        verify(eventPublisher, times(3))
                .publishEvent(any(PaymentStatusChangedEvent.class)); // PENDING, PROCESSING, COMPLETED
    }

    @Test
    void rejectedRiskDecisionFailsWithoutCallingProvider() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
        when(riskAssessmentPort.assess(999_999L, "TRY")).thenReturn(RiskDecision.reject("suspicious amount"));

        PaymentCreationOutcome outcome = paymentService.createPayment("key-2", 999_999L, "TRY");

        assertThat(outcome.payment().getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(outcome.payment().getFailureReason()).isEqualTo("suspicious amount");
        verify(paymentProviderClient, never()).charge(anyLong(), anyString());
    }

    @Test
    void providerFailureMarksPaymentFailed() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByIdempotencyKey("key-3")).thenReturn(Optional.empty());
        when(riskAssessmentPort.assess(5_000L, "TRY")).thenReturn(RiskDecision.approve());
        when(paymentProviderClient.charge(5_000L, "TRY")).thenReturn(ProviderResult.failure("provider down"));

        PaymentCreationOutcome outcome = paymentService.createPayment("key-3", 5_000L, "TRY");

        assertThat(outcome.payment().getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(outcome.payment().getFailureReason()).isEqualTo("provider down");
    }

    @Test
    void replayingSameIdempotencyKeyDoesNotCreateANewPayment() {
        Payment existing = Payment.create("key-4", 1_000L, "TRY");
        when(paymentRepository.findByIdempotencyKey("key-4")).thenReturn(Optional.of(existing));

        PaymentCreationOutcome outcome = paymentService.createPayment("key-4", 1_000L, "TRY");

        assertThat(outcome.newlyCreated()).isFalse();
        assertThat(outcome.payment()).isSameAs(existing);
        verify(riskAssessmentPort, never()).assess(anyLong(), anyString());
        verify(paymentProviderClient, never()).charge(anyLong(), anyString());
    }
}
