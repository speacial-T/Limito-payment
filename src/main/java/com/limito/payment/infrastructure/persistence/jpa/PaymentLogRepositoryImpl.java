package com.limito.payment.infrastructure.persistence.jpa;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;
import com.limito.payment.domain.model.PaymentLogEntity;
import com.limito.payment.domain.repository.PaymentLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public Optional<String> findIdempotencyKey(UUID paymentId,
                                               PaymentStatusEnum status,
                                               RefundStatusEnum refundStatus) {
        return paymentLogJpaRepository.findIdempotencyKeyByStatus(paymentId, status,
                refundStatus);
    }

    @Override
    public int findMaxTryCount(UUID paymentId,
                               PaymentStatusEnum status,
                               RefundStatusEnum refundStatus) {
        return paymentLogJpaRepository.findMaxRetryCount(paymentId,
                status, refundStatus);
    }


}
