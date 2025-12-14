package com.limito.payment.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.limito.payment.domain.model.PaymentLogEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class PaymentLogRepositoryImpl implements PaymentLogRepository {
	private final PaymentLogJpaRepository paymentLogJpaRepository;

	@Override
	public PaymentLogEntity save(PaymentLogEntity paymentLog) {
		return paymentLogJpaRepository.save(paymentLog);
	}

	@Override
	public List<PaymentLogEntity> findAllByPaymentPaymentId(UUID paymentId) {
		return paymentLogJpaRepository.findAllByPaymentPaymentId(paymentId);
	}
}
