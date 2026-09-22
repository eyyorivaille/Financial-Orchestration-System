package com.financial.project.notification.internal;

import com.financial.project.payment.PaymentStatus;
import com.financial.project.payment.PaymentStatusChangedEvent;
import com.financial.project.user.UserProfileApi;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
class NotificationService {

    private static final Set<PaymentStatus> TERMINAL_STATUSES =
            EnumSet.of(PaymentStatus.COMPLETED, PaymentStatus.FAILED);

    private final NotificationSender notificationSender;
    private final NotificationRepository notificationRepository;
    private final UserProfileApi userProfileApi;

    NotificationService(
            NotificationSender notificationSender,
            NotificationRepository notificationRepository,
            UserProfileApi userProfileApi) {
        this.notificationSender = notificationSender;
        this.notificationRepository = notificationRepository;
        this.userProfileApi = userProfileApi;
    }

    void handlePaymentStatusChanged(PaymentStatusChangedEvent event) {
        if (!TERMINAL_STATUSES.contains(event.status())) {
            return;
        }

        // The profile is synced synchronously (JwtProfileSyncFilter) on the request
        // that created the payment, so it's normally already there by the time this
        // async listener runs; fall back to a placeholder if it somehow isn't.
        String recipient = userProfileApi
                .findContact(event.customerId())
                .map(contact -> contact.email())
                .orElse(event.customerId() + "@example.com");
        String amount = formatAmount(event.amountMinorUnits(), event.currency());
        String subject;
        String message;
        if (event.status() == PaymentStatus.COMPLETED) {
            subject = "Payment completed";
            message = "Your payment of %s was completed successfully.".formatted(amount);
        } else {
            subject = "Payment failed";
            message = "Your payment of %s failed: %s".formatted(amount, event.reason());
        }

        NotificationResult result = notificationSender.send(recipient, subject, message);
        notificationRepository.save(Notification.record(
                event.paymentId(),
                event.customerId(),
                NotificationChannel.EMAIL,
                recipient,
                subject,
                message,
                result.status()));
    }

    private String formatAmount(long amountMinorUnits, String currency) {
        // Locale.ROOT: financial output must not vary with the server's default locale (e.g. "," vs "." decimals).
        return String.format(Locale.ROOT, "%.2f %s", amountMinorUnits / 100.0, currency);
    }
}
