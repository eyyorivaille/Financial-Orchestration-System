package com.financial.project.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PaymentStatusTest {

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    void createdCanOnlyMoveToPendingFailedOrCancelled(PaymentStatus target) {
        boolean expected = EnumSet.of(PaymentStatus.PENDING, PaymentStatus.FAILED, PaymentStatus.CANCELLED)
                .contains(target);
        assertThat(PaymentStatus.CREATED.canTransitionTo(target)).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    void pendingCanOnlyMoveToProcessingOrCancelled(PaymentStatus target) {
        boolean expected =
                EnumSet.of(PaymentStatus.PROCESSING, PaymentStatus.CANCELLED).contains(target);
        assertThat(PaymentStatus.PENDING.canTransitionTo(target)).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    void processingCanOnlyMoveToCompletedOrFailed(PaymentStatus target) {
        boolean expected =
                EnumSet.of(PaymentStatus.COMPLETED, PaymentStatus.FAILED).contains(target);
        assertThat(PaymentStatus.PROCESSING.canTransitionTo(target)).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(
            value = PaymentStatus.class,
            names = {"COMPLETED", "FAILED", "CANCELLED"})
    void terminalStatesHaveNoOutgoingTransitions(PaymentStatus terminal) {
        for (PaymentStatus target : PaymentStatus.values()) {
            assertThat(terminal.canTransitionTo(target)).isFalse();
        }
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    void assertTransitionAllowedThrowsOnInvalidTransition(PaymentStatus from) {
        for (PaymentStatus to : PaymentStatus.values()) {
            if (from.canTransitionTo(to)) {
                continue;
            }
            assertThatThrownBy(() -> from.assertTransitionAllowed(to))
                    .isInstanceOf(IllegalStateTransitionException.class);
        }
    }
}
