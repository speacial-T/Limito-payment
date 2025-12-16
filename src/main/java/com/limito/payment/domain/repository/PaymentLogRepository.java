package com.limito.payment.domain.repository;

import com.limito.payment.domain.model.PaymentLogEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentLogRepository {

    PaymentLogEntity save(PaymentLogEntity paymentLog);

    List<PaymentLogEntity> findAllByPaymentPaymentId(UUID paymentId);

    Optional<Integer> findMaxRetryCountByPaymentKey(String paymentKey);

    void saveAll(List<PaymentLogEntity> list);
}
