package com.limito.payment.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.ProductTypeEnum;
import com.limito.payment.domain.model.PaymentItemEntity;

public interface PaymentItemRepository {
	@Transactional
	List<PaymentItemEntity> saveAll(List<PaymentItemEntity> updatedProducts);

	@Transactional(readOnly = true)
	List<PaymentItemEntity> getPaymentItems(UUID paymentId);

	@Transactional(readOnly = true)
	ProductTypeEnum getProductTypeByPaymentId(UUID paymentId);

	@Transactional
	int updateStatusByPaymentId(UUID paymentId, PaymentStatusEnum status);
}
