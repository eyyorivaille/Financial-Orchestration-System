package com.financial.project.payment.internal;

import com.financial.project.payment.PaymentCreatedEvent;
import com.financial.project.payment.PaymentStatus;
import com.financial.project.payment.PaymentStatusChangedEvent;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RiskAssessmentPort riskAssessmentPort;
    private final PaymentProviderClient paymentProviderClient;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            RiskAssessmentPort riskAssessmentPort,
            PaymentProviderClient paymentProviderClient,
            ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.riskAssessmentPort = riskAssessmentPort;
        this.paymentProviderClient = paymentProviderClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentCreationOutcome createPayment(String idempotencyKey, long amountMinorUnits, String currency) {
        return paymentRepository
                .findByIdempotencyKey(idempotencyKey)
                .map(existing -> new PaymentCreationOutcome(existing, false))
                .orElseGet(() -> new PaymentCreationOutcome(
                        processNewPayment(idempotencyKey, amountMinorUnits, currency), true));
    }

    private Payment processNewPayment(String idempotencyKey, long amountMinorUnits, String currency) {
        RiskDecision riskDecision = riskAssessmentPort.assess(amountMinorUnits, currency);

        Payment payment = Payment.create(idempotencyKey, amountMinorUnits, currency);
        payment = paymentRepository.save(payment);
        eventPublisher.publishEvent(
                new PaymentCreatedEvent(payment.getId(), amountMinorUnits, currency, Instant.now()));

        if (!riskDecision.isApproved()) {
            payment.markFailed(riskDecision.reason());
            return saveAndPublishStatus(payment);
        }

        payment.transitionTo(PaymentStatus.PENDING);
        payment = saveAndPublishStatus(payment);

        payment.transitionTo(PaymentStatus.PROCESSING);
        payment = saveAndPublishStatus(payment);

        ProviderResult result = paymentProviderClient.charge(amountMinorUnits, currency);
        if (result.successful()) {
            payment.transitionTo(PaymentStatus.COMPLETED);
        } else {
            payment.markFailed(result.failureReason());
        }
        return saveAndPublishStatus(payment);
    }

    private Payment saveAndPublishStatus(Payment payment) {
        payment = paymentRepository.save(payment);
        eventPublisher.publishEvent(new PaymentStatusChangedEvent(
                payment.getId(), payment.getStatus(), payment.getFailureReason(), Instant.now()));
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getPayment(UUID id) {
        return paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }
}
