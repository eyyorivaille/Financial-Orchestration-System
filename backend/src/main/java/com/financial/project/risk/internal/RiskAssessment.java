package com.financial.project.risk.internal;

import com.financial.project.risk.RiskAssessmentRequest;
import com.financial.project.risk.RiskOutcome;
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
@Table(name = "risk_assessment")
@Getter
public class RiskAssessment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "amount_minor_units", nullable = false)
    private long amountMinorUnits;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "score", nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    private RiskOutcome outcome;

    @Column(name = "reason")
    private String reason;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

    protected RiskAssessment() {
        // JPA
    }

    public static RiskAssessment record(RiskAssessmentRequest request, int score, RiskOutcome outcome, String reason) {
        RiskAssessment assessment = new RiskAssessment();
        assessment.customerId = request.customerId();
        assessment.amountMinorUnits = request.amountMinorUnits();
        assessment.currency = request.currency();
        assessment.countryCode = request.countryCode();
        assessment.score = score;
        assessment.outcome = outcome;
        assessment.reason = reason;
        assessment.assessedAt = Instant.now();
        return assessment;
    }
}
