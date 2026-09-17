package com.financial.project.notification.internal;

public interface NotificationSender {

    NotificationResult send(String recipient, String subject, String message);
}
