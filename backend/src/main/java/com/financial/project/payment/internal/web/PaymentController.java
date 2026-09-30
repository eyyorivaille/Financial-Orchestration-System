package com.financial.project.payment.internal.web;

import com.financial.project.payment.internal.Payment;
import com.financial.project.payment.internal.PaymentCreationOutcome;
import com.financial.project.payment.internal.PaymentNotFoundException;
import com.financial.project.payment.internal.PaymentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
class PaymentController {

    private final PaymentService paymentService;

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        PaymentCreationOutcome outcome = paymentService.createPayment(
                idempotencyKey,
                request.amountMinorUnits(),
                request.currency(),
                jwt.getSubject(),
                request.countryCode());
        PaymentResponse response = PaymentResponse.from(outcome.payment());
        HttpStatus status = outcome.newlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status)
                .location(URI.create("/api/payments/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        Payment payment = paymentService.getPayment(id);
        if (!payment.getCustomerId().equals(jwt.getSubject()) && !hasOpsRole(jwt)) {
            // Don't reveal that a payment with this id exists to a non-owner.
            throw new PaymentNotFoundException(id);
        }
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    private boolean hasOpsRole(Jwt jwt) {
        if (!(jwt.getClaim("realm_access") instanceof Map<?, ?> realmAccess)) {
            return false;
        }
        return realmAccess.get("roles") instanceof List<?> roles && roles.contains("ops");
    }
}
