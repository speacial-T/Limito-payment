package com.limito.payment.domain.dto;

import java.util.UUID;

import com.limito.payment.domain.enums.CancelAndRefundStatusEnum;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.ProductTypeEnum;

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
public class PaymentItemDetailDtoV1 {

	CancelAndRefundStatusEnum cancelAndRefundStatus;
	private UUID paymentItemId;
	private PaymentDetailDtoV1 payment;
	private Long sellerId;
	private UUID orderItemId;
	private ProductTypeEnum productType;
	private String productName;
	private int productPrice;
	private int productAmount;
	private Integer refundPrice;
	private PaymentStatusEnum status;

}
