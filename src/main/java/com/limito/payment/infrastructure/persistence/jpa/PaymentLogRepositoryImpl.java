package com.limito.payment.infrastructure.persistence.jpa;

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
    public Optional<Integer> findMaxRetryCountByPaymentKey(String paymentKey) {
        return paymentLogJpaRepository.findMaxRetryCountByPaymentKey(paymentKey);
    }

    @Override
    public void saveAll(List<PaymentLogEntity> list) {
        paymentLogJpaRepository.saveAll(list);
    }
}
