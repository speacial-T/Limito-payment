package com.limito.payment.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;
import com.limito.payment.domain.model.PaymentLogEntity;

public interface PaymentLogRepository {

	PaymentLogEntity save(PaymentLogEntity paymentLog);

	List<PaymentLogEntity> findAllByPaymentPaymentId(UUID paymentId);

	int findMaxTryCount(UUID paymentId,
		PaymentStatusEnum status,
		RefundStatusEnum refundStatus);

	Optional<String> findIdempotencyKey(UUID paymentId,
		PaymentStatusEnum status,
		RefundStatusEnum refundStatus);

	void saveAll(List<PaymentLogEntity> list);

}
