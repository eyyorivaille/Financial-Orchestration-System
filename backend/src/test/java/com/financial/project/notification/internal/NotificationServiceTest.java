package com.financial.project.notification.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financial.project.payment.PaymentStatus;
import com.financial.project.payment.PaymentStatusChangedEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationSender notificationSender;

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @Test
    void nonTerminalStatusIsIgnored() {
        notificationService = new NotificationService(notificationSender, notificationRepository);

        notificationService.handlePaymentStatusChanged(event(PaymentStatus.PENDING, null));

        verify(notificationSender, never()).send(any(), any(), any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void completedPaymentSendsSuccessNotification() {
        notificationService = new NotificationService(notificationSender, notificationRepository);
        when(notificationSender.send(any(), any(), any())).thenReturn(NotificationResult.sent());

        notificationService.handlePaymentStatusChanged(event(PaymentStatus.COMPLETED, null));

        verify(notificationSender, times(1)).send(eq("customer-1@example.com"), eq("Payment completed"), any());
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(captor.getValue().getMessage()).contains("15.00 TRY");
    }

    @Test
    void failedPaymentSendsFailureNotificationWithReason() {
        notificationService = new NotificationService(notificationSender, notificationRepository);
        when(notificationSender.send(any(), any(), any())).thenReturn(NotificationResult.sent());

        notificationService.handlePaymentStatusChanged(event(PaymentStatus.FAILED, "provider down"));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationSender).send(any(), eq("Payment failed"), messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("provider down");
    }

    private PaymentStatusChangedEvent event(PaymentStatus status, String reason) {
        return new PaymentStatusChangedEvent(
                UUID.randomUUID(), "customer-1", 1_500L, "TRY", status, reason, Instant.now());
    }
}
