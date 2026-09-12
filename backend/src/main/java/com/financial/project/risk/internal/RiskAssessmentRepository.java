package com.financial.project.risk.internal;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {

    long countByCustomerIdAndAssessedAtAfter(String customerId, Instant since);
}
