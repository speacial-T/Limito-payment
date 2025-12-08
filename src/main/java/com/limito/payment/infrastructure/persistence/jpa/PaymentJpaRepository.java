package com.limito.payment.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.limito.payment.domain.model.PaymentEntity;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

	Optional<PaymentEntity> findByOrderIdAndDeletedAtIsNull(UUID orderId);

	boolean existsByOrderIdAndDeletedAtIsNull(UUID orderId);

	@Query("SELECT p FROM PaymentEntity p JOIN FETCH p.items WHERE p.orderId = :orderId")
	Optional<PaymentEntity> findByOrderIdWithItems(@Param("orderId") UUID orderId);

}
