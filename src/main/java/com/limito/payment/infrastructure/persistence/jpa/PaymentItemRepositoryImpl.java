package com.limito.payment.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.ProductTypeEnum;
import com.limito.payment.domain.model.PaymentItemEntity;
import com.limito.payment.domain.model.PaymentItemMapper;
import com.limito.payment.domain.model.PaymentMapper;
import com.limito.payment.domain.repository.PaymentItemRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class PaymentItemRepositoryImpl implements PaymentItemRepository {

	private final PaymentItemJpaRepository paymentItemJpaRepository;
	private final PaymentMapper mapper;
	private final PaymentItemMapper itemMapper;

	@Transactional
	@Override
	public List<PaymentItemEntity> saveAll(List<PaymentItemEntity> orderItems) {
		return paymentItemJpaRepository.saveAll(orderItems);
	}

	@Transactional(readOnly = true)
	@Override
	public List<PaymentItemEntity> getPaymentItems(UUID paymentId) {
		List<PaymentItemEntity> paymentItems = paymentItemJpaRepository.findAllByPaymentPaymentId(paymentId);

		if (paymentItems.isEmpty()) {
			throw new IllegalArgumentException("No payment items found for paymentId: " + paymentId);
		}
		return paymentItems;
	}

	@Override
	public ProductTypeEnum getProductTypeByPaymentId(UUID paymentId) {
		return paymentItemJpaRepository.findProductTypeByPaymentId(paymentId);
	}

}
