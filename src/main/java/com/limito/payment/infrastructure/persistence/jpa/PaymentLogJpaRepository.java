package com.limito.payment.infrastructure.persistence.jpa;

import com.limito.payment.domain.model.PaymentLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentLogJpaRepository extends JpaRepository<PaymentLogEntity, UUID> {
    @Query("""
            SELECT pl
            FROM PaymentLogEntity pl
            WHERE pl.payment_id = :paymentId
            """)
    List<PaymentLogEntity> findAllByPaymentPaymentId(UUID paymentId);

    Optional<Integer> findMaxRetryCountByPaymentKey(String paymentKey);
}
