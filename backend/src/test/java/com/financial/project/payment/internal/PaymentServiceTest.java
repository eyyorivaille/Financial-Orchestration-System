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
import com.financial.project.risk.RiskAssessmentApi;
import com.financial.project.risk.RiskAssessmentRequest;
import com.financial.project.risk.RiskDecision;
import com.financial.project.risk.RiskOutcome;
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
    private RiskAssessmentApi riskAssessmentApi;

    @Mock
    private PaymentProviderClient paymentProviderClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService =
                new PaymentService(paymentRepository, riskAssessmentApi, paymentProviderClient, eventPublisher);
    }

    @Test
    void approvedPaymentGoesThroughToCompleted() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(riskAssessmentApi.assess(any(RiskAssessmentRequest.class)))
                .thenReturn(new RiskDecision(RiskOutcome.APPROVE, 0, null));
        when(paymentProviderClient.charge(10_000L, "TRY")).thenReturn(ProviderResult.success());

        PaymentCreationOutcome outcome = paymentService.createPayment("key-1", 10_000L, "TRY", "customer-1", "TR");

        assertThat(outcome.newlyCreated()).isTrue();
        assertThat(outcome.payment().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(eventPublisher, times(1)).publishEvent(any(PaymentCreatedEvent.class));
        verify(eventPublisher, times(3))
                .publishEvent(any(PaymentStatusChangedEvent.class)); // PENDING, PROCESSING, COMPLETED
    }

    @Test
    void reviewRiskDecisionIsTreatedAsApproved() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByIdempotencyKey("key-review")).thenReturn(Optional.empty());
        when(riskAssessmentApi.assess(any(RiskAssessmentRequest.class)))
                .thenReturn(new RiskDecision(RiskOutcome.REVIEW, 40, "large amount"));
        when(paymentProviderClient.charge(10_000L, "TRY")).thenReturn(ProviderResult.success());

        PaymentCreationOutcome outcome = paymentService.createPayment("key-review", 10_000L, "TRY", "customer-1", "TR");

        assertThat(outcome.payment().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void rejectedRiskDecisionFailsWithoutCallingProvider() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
        when(riskAssessmentApi.assess(any(RiskAssessmentRequest.class)))
                .thenReturn(new RiskDecision(RiskOutcome.REJECT, 90, "suspicious amount"));

        PaymentCreationOutcome outcome = paymentService.createPayment("key-2", 20_000_000L, "TRY", "customer-1", "TR");

        assertThat(outcome.payment().getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(outcome.payment().getFailureReason()).contains("suspicious amount");
        verify(paymentProviderClient, never()).charge(anyLong(), anyString());
    }

    @Test
    void providerFailureMarksPaymentFailed() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.findByIdempotencyKey("key-3")).thenReturn(Optional.empty());
        when(riskAssessmentApi.assess(any(RiskAssessmentRequest.class)))
                .thenReturn(new RiskDecision(RiskOutcome.APPROVE, 0, null));
        when(paymentProviderClient.charge(5_000L, "TRY")).thenReturn(ProviderResult.failure("provider down"));

        PaymentCreationOutcome outcome = paymentService.createPayment("key-3", 5_000L, "TRY", "customer-1", "TR");

        assertThat(outcome.payment().getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(outcome.payment().getFailureReason()).isEqualTo("provider down");
    }

    @Test
    void replayingSameIdempotencyKeyDoesNotCreateANewPayment() {
        Payment existing = Payment.create("key-4", 1_000L, "TRY");
        when(paymentRepository.findByIdempotencyKey("key-4")).thenReturn(Optional.of(existing));

        PaymentCreationOutcome outcome = paymentService.createPayment("key-4", 1_000L, "TRY", "customer-1", "TR");

        assertThat(outcome.newlyCreated()).isFalse();
        assertThat(outcome.payment()).isSameAs(existing);
        verify(riskAssessmentApi, never()).assess(any(RiskAssessmentRequest.class));
        verify(paymentProviderClient, never()).charge(anyLong(), anyString());
    }
}
