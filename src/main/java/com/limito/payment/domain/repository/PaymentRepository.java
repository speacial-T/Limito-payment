package com.limito.payment.domain.repository;

import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.limito.payment.domain.model.PaymentEntity;

public interface PaymentRepository {
	@Transactional()
	PaymentEntity getByOrderId(UUID orderId);

	@Transactional()
	PaymentEntity save(PaymentEntity payment);

	@Transactional(readOnly = true)
	boolean hasPaymentByOrderId(UUID orderId);

}
