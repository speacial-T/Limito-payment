package com.limito.payment.domain.repository;

import com.limito.payment.domain.model.PaymentEntity;

import java.util.UUID;

public interface PaymentRepository {

    PaymentEntity findByOrderId(UUID orderId);

    PaymentEntity save(PaymentEntity payment);

    boolean hasPaymentByOrderId(UUID orderId);
}
