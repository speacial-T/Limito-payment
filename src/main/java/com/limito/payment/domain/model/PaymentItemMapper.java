package com.limito.payment.domain.model;

import org.springframework.stereotype.Component;

import com.limito.payment.domain.dto.PaymentItemDetailDtoV1;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.presentation.dto.request.OrderItem;

@Component
public class PaymentItemMapper {

	public PaymentItemDetailDtoV1 mapToPaymentItem(OrderItem orderItem) {

		return PaymentItemDetailDtoV1.builder()
			.sellerId(orderItem.getSellerId())
			.orderItemId(orderItem.getOptionId())
			.productType(orderItem.getProductType())
			.productName(orderItem.getProductName())
			.productPrice(orderItem.getProductPrice())
			.productAmount(orderItem.getQuantity())
			.status(PaymentStatusEnum.IN_PROGRESS)
			.build();
	}

	public PaymentItemDetailDtoV1 toDto(PaymentItemEntity entity) {
		if (entity == null)
			return null;

		return PaymentItemDetailDtoV1.builder()
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

	public PaymentItemEntity toEntity(PaymentItemDetailDtoV1 dto) {
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