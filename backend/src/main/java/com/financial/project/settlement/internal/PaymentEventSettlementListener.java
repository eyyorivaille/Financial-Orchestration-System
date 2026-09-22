package com.financial.project.settlement.internal;

import com.financial.project.payment.PaymentCreatedEvent;
import com.financial.project.payment.PaymentStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes the same "payment-events" topic as notification/audit, with its own
 * consumer group ("settlement-module") so it gets an independent copy of every event.
 */
@Component
@KafkaListener(
        topics = "payment-events",
        groupId = "settlement-module",
        containerFactory = "paymentEventsKafkaListenerContainerFactory")
class PaymentEventSettlementListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventSettlementListener.class);

    private final SettlementService settlementService;

    PaymentEventSettlementListener(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @KafkaHandler
    void onPaymentCreated(PaymentCreatedEvent event) {
        log.debug("Ignoring PaymentCreatedEvent for settlement: {}", event);
    }

    @KafkaHandler
    void onPaymentStatusChanged(PaymentStatusChangedEvent event) {
        settlementService.handlePaymentStatusChanged(event);
    }

    @KafkaHandler(isDefault = true)
    void onOther(Object event) {
        log.debug("Ignoring unrecognized payment event for settlement: {}", event);
    }
}
