package com.limito.payment.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.ProductTypeEnum;
import com.limito.payment.domain.model.PaymentItemEntity;

public interface PaymentItemJpaRepository extends JpaRepository<PaymentItemEntity, UUID> {

	@Query("""
		SELECT pi
		FROM PaymentItemEntity pi
		WHERE pi.payment.paymentId = :paymentId
		""")
	/*@Query("""
		SELECT pi
		FROM PaymentItemEntity pi
		WHERE pi.payment.paymentId = :paymentId
		AND pi.deletedAt IS NULL
		""")*/
	List<PaymentItemEntity> findAllByPaymentPaymentId(UUID paymentId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		update PaymentItemEntity pi
		set pi.status = :status
		where pi.payment.paymentId = :paymentId
		""")
	int updateStatusByPaymentId(UUID paymentId, PaymentStatusEnum status);

	@Query("""
		select pi.productType
		from PaymentItemEntity pi
		where pi.payment.paymentId = :paymentId
		group by pi.productType
		""")
	ProductTypeEnum findProductTypeByPaymentId(UUID paymentId);
}
