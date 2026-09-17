package com.financial.project.notification.internal;

import com.financial.project.payment.PaymentCreatedEvent;
import com.financial.project.payment.PaymentStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes the "payment-events" Kafka topic (both PaymentCreatedEvent and
 * PaymentStatusChangedEvent land here - the notificationKafkaListenerContainerFactory
 * (see KafkaMessageConverterConfig) resolves the concrete type from the
 * __TypeId__ header Spring Modulith's producer sets, and @KafkaHandler
 * dispatches by that type).
 */
@Component
@KafkaListener(
        topics = "payment-events",
        groupId = "notification-module",
        containerFactory = "notificationKafkaListenerContainerFactory")
class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final NotificationService notificationService;

    PaymentEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaHandler
    void onPaymentCreated(PaymentCreatedEvent event) {
        log.debug("Payment created: {}", event.paymentId());
    }

    @KafkaHandler
    void onPaymentStatusChanged(PaymentStatusChangedEvent event) {
        notificationService.handlePaymentStatusChanged(event);
    }

    @KafkaHandler(isDefault = true)
    void onOther(Object event) {
        log.debug("Ignoring unrecognized payment event: {}", event);
    }
}
