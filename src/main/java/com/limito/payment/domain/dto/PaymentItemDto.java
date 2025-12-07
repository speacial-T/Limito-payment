package com.limito.payment.domain.dto;

import java.util.UUID;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.ProductTypeEnum;
import com.limito.payment.presentation.dto.request.OrderItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class PaymentItemDto {

	private UUID paymentItemId;

	private PaymentDto payment;

	private Long sellerId;

	private UUID orderItemId;

	private ProductTypeEnum productType;

	private String productName;

	private int productPrice;

	private int productAmount;

	private Integer refundPrice;

	private PaymentStatusEnum status;

	public static PaymentItemDto mapToPaymentItem(OrderItem orderItem) {

		return PaymentItemDto.builder()
			.sellerId(orderItem.getSellerId())
			.orderItemId(orderItem.getOptionId())
			.productType(orderItem.getProductType())
			.productName(orderItem.getProductName())
			.productPrice(orderItem.getProductPrice())
			.productAmount(orderItem.getQuantity())
			.status(PaymentStatusEnum.IN_PROGRESS)
			.build();
	}

	public void updateStatusBasedOnPayment(PaymentStatusEnum paymentStatus) {
		this.status = paymentStatus;
	}
}
