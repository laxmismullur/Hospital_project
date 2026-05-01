package com.lm.hospital.repository;

import com.lm.hospital.model.LMBilling;
import com.lm.hospital.model.LMPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LMBillingRepository extends JpaRepository<LMBilling, Long> {
    List<LMBilling> findByPatientId(Long patientId);
    List<LMBilling> findByPaymentStatus(LMPaymentStatus status);
    Optional<LMBilling> findByInvoiceNumber(String invoiceNumber);
    long countByPaymentStatus(LMPaymentStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM LMBilling b")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM LMBilling b")
    BigDecimal sumPaidRevenue();
}
