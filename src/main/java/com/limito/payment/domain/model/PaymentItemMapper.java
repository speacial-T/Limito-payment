package com.limito.payment.domain.model;

import org.springframework.stereotype.Component;

import com.limito.payment.domain.dto.PaymentItemDto;

@Component
public class PaymentItemMapper {

	public PaymentItemDto toDto(PaymentItemEntity entity) {
		if (entity == null)
			return null;

		return PaymentItemDto.builder()
			.paymentItemId(entity.paymentItemId)
			.sellerId(entity.sellerId)
			.orderItemId(entity.orderItemId)
			.productType(entity.productType)
			.productName(entity.productName)
			.productPrice(entity.productPrice != null ? entity.productPrice : 0)
			.productAmount(entity.productAmount != null ? entity.productAmount : 0)
			.refundPrice(entity.refundPrice)
			.status(entity.status)
			.build();
	}

	public PaymentItemEntity toEntity(PaymentItemDto dto) {
		if (dto == null)
			return null;

		return new PaymentItemEntity(
			dto.getPaymentItemId(),
			null, // payment는 순환 참조 방지를 위해 제외
			dto.getSellerId(),
			dto.getOrderItemId(),
			dto.getProductName(),
			Integer.valueOf(dto.getProductPrice()),
			dto.getProductType(),
			Integer.valueOf(dto.getProductAmount()),
			dto.getRefundPrice(),
			dto.getStatus()
		);
	}
}