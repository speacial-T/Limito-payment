package com.limito.payment.infrastructure.persistence.jpa;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;
import com.limito.payment.domain.model.PaymentLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentLogJpaRepository extends JpaRepository<PaymentLogEntity, UUID> {
    @Query("""
            SELECT pl
            FROM PaymentLogEntity pl
            WHERE pl.paymentId = :paymentId order by pl.createdAt DESC 
            """)
    List<PaymentLogEntity> findAllByPaymentPaymentId(UUID paymentId);

    @Query("""
            	select coalesce(max(p.tryCount), 0)
            	from PaymentLogEntity p
            	where p.paymentId = :paymentId
            	  and p.paymentStatus = :paymentStatus
            	  and p.refundStatus = :refundStatus
            """)
    int findMaxRetryCount(
            @Param("paymentId") UUID paymentId,
            @Param("paymentStatus") PaymentStatusEnum paymentStatus,
            @Param("refundStatus") RefundStatusEnum refundStatus
    );

    @Query("""
            	select p.idempotencyKey
            	from PaymentLogEntity p
            	where p.paymentId = :paymentId
            	  and p.paymentStatus = :paymentStatus
            	  and p.refundStatus = :refundStatus
            	order by p.createdAt desc
            """)
    Optional<String> findIdempotencyKeyByStatus(
            @Param("paymentId") UUID paymentId,
            @Param("paymentStatus") PaymentStatusEnum paymentStatus,
            @Param("refundStatus") RefundStatusEnum refundStatus
    );

}
