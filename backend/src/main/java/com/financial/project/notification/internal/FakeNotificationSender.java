package com.financial.project.notification.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simulates an email/SMS gateway by logging instead of calling a real provider
 * (no Twilio/SendGrid integration - out of scope for this project). Unlike the
 * fake payment provider, this never fails, so no circuit breaker is applied here.
 */
@Component
class FakeNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(FakeNotificationSender.class);

    @Override
    public NotificationResult send(String recipient, String subject, String message) {
        log.info("Simulated notification sent to {} - subject='{}', message='{}'", recipient, subject, message);
        return NotificationResult.sent();
    }
}
