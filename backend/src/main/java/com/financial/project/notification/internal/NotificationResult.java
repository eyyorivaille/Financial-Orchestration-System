package com.financial.project.notification.internal;

public record NotificationResult(NotificationStatus status, String failureReason) {

    public static NotificationResult sent() {
        return new NotificationResult(NotificationStatus.SENT, null);
    }

    public static NotificationResult failed(String reason) {
        return new NotificationResult(NotificationStatus.FAILED, reason);
    }
}
