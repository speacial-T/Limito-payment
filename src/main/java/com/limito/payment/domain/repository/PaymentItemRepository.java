package com.limito.payment.domain.repository;

import com.limito.payment.domain.enums.ProductTypeEnum;
import com.limito.payment.domain.model.PaymentItemEntity;

import java.util.List;
import java.util.UUID;

public interface PaymentItemRepository {

    List<PaymentItemEntity> saveAll(List<PaymentItemEntity> updatedProducts);

    List<PaymentItemEntity> getPaymentItems(UUID paymentId);

    ProductTypeEnum getProductTypeByPaymentId(UUID paymentId);

}
