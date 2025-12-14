package com.limito.payment.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.limito.payment.domain.model.PaymentLogEntity;

public interface PaymentLogJpaRepository extends JpaRepository<PaymentLogEntity, UUID> {
	@Query("""
		SELECT pl
		FROM PaymentLogEntity pl
		WHERE pl.payment.paymentId = :paymentId
		""")
	List<PaymentLogEntity> findAllByPaymentPaymentId(UUID paymentId);
}
