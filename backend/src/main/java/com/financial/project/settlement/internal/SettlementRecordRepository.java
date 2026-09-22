package com.financial.project.settlement.internal;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SettlementRecordRepository extends JpaRepository<SettlementRecord, UUID> {

    List<SettlementRecord> findByPaymentId(UUID paymentId);
}
