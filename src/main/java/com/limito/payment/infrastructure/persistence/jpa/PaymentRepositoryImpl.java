package com.limito.payment.infrastructure.persistence.jpa;

import static com.limito.payment.domain.exception.PaymentErrorCode.*;

import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.limito.common.exception.AppException;
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
	public PaymentEntity findByOrderId(UUID orderId) {
		return paymentJpaRepository.findByOrderIdWithItems(orderId)
			.orElseThrow(() -> AppException.of(PAYMENT_NOT_FOUND));

	}

	@Transactional()
	@Override
	public PaymentEntity save(PaymentEntity payment) {
		return paymentJpaRepository.save(payment);
	}

	@Override
	public boolean hasPaymentByOrderId(UUID orderId) {
		return paymentJpaRepository.existsByOrderId(orderId);
	}

}
