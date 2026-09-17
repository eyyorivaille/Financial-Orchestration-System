package com.financial.project.audit.internal;

import com.financial.project.payment.PaymentCreatedEvent;
import com.financial.project.payment.PaymentStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes the same "payment-events" topic as the notification module, but
 * with its own consumer group ("audit-module") so it gets an independent copy
 * of every event - and unlike notification, records ALL of them (not just
 * terminal statuses), since the audit trail is meant to be complete.
 */
@Component
@KafkaListener(
        topics = "payment-events",
        groupId = "audit-module",
        containerFactory = "paymentEventsKafkaListenerContainerFactory")
class PaymentEventAuditListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventAuditListener.class);

    private final AuditLogService auditLogService;

    PaymentEventAuditListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @KafkaHandler
    void onPaymentCreated(PaymentCreatedEvent event) {
        auditLogService.record(event, "PaymentCreatedEvent", event.customerId(), event.occurredAt());
    }

    @KafkaHandler
    void onPaymentStatusChanged(PaymentStatusChangedEvent event) {
        auditLogService.record(event, "PaymentStatusChangedEvent", event.customerId(), event.occurredAt());
    }

    @KafkaHandler(isDefault = true)
    void onOther(Object event) {
        log.debug("Ignoring unrecognized payment event for audit: {}", event);
    }
}
