package com.limito.payment.domain.repository;

import java.util.UUID;

import com.limito.payment.domain.model.PaymentEntity;

public interface PaymentRepository {

	PaymentEntity findByOrderId(UUID orderId);

	PaymentEntity save(PaymentEntity payment);

	boolean hasPaymentByOrderId(UUID orderId);
}
