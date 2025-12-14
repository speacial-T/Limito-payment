package com.limito.payment.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.limito.payment.domain.model.PaymentLogEntity;

public interface PaymentLogRepository {

	@Transactional()
	PaymentLogEntity save(PaymentLogEntity paymentLog);

	@Transactional(readOnly = true)
	List<PaymentLogEntity> findByPaymentId(UUID paymentId);
}
