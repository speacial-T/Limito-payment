package com.limito.payment.infrastructure.persistence.jpa;

import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.limito.payment.domain.model.PaymentEntity;
import com.limito.payment.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
	private final PaymentJpaRepository paymentJpaRepository;

	@Transactional(readOnly = true)
	@Override
	public PaymentEntity getByOrderId(UUID orderId) {
		return paymentJpaRepository.findByOrderIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new IllegalArgumentException("Payment not found for orderId: " + orderId));

	}

	@Transactional()
	@Override
	public PaymentEntity save(PaymentEntity payment) {
		return paymentJpaRepository.save(payment);
	}

	public PaymentEntity findById(UUID paymentId) {
		PaymentEntity paymentEntity = paymentJpaRepository.findById(paymentId)
			.orElseThrow(() -> new IllegalArgumentException("Payment not found for paymentId: " + paymentId));
		return paymentEntity;
	}

	@Override
	public boolean hasPaymentByOrderId(UUID orderId) {
		return paymentJpaRepository.existsByOrderIdAndDeletedAtIsNull(orderId);
	}

}
