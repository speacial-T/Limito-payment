package com.limito.payment.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.limito.payment.domain.model.PaymentItemEntity;

public interface PaymentItemJpaRepository extends JpaRepository<PaymentItemEntity, UUID> {

	@Query("SELECT pi FROM PaymentItemEntity pi WHERE pi.payment.paymentId = :paymentId AND pi.deletedAt IS NULL")
	List<PaymentItemEntity> findAllByPaymentPaymentId(UUID paymentId);
}
