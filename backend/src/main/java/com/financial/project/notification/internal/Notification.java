package com.financial.project.notification.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "notification")
@Getter
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "message", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Notification() {
        // JPA
    }

    public static Notification record(
            UUID paymentId,
            String customerId,
            NotificationChannel channel,
            String recipient,
            String subject,
            String message,
            NotificationStatus status) {
        Notification notification = new Notification();
        notification.paymentId = paymentId;
        notification.customerId = customerId;
        notification.channel = channel;
        notification.recipient = recipient;
        notification.subject = subject;
        notification.message = message;
        notification.status = status;
        notification.createdAt = Instant.now();
        return notification;
    }
}
