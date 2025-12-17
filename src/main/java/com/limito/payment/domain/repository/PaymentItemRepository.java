package com.limito.payment.domain.repository;

import java.util.List;
import java.util.UUID;

import com.limito.payment.domain.enums.ProductTypeEnum;
import com.limito.payment.domain.model.PaymentItemEntity;

public interface PaymentItemRepository {

	void saveAll(List<PaymentItemEntity> updatedProducts);

	List<PaymentItemEntity> getPaymentItems(UUID paymentId);

	ProductTypeEnum getProductTypeByPaymentId(UUID paymentId);

}
