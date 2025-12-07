package com.limito.payment.domain.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.limito.payment.domain.enums.CancelAndRefundStatusEnum;
import com.limito.payment.domain.enums.PaymentMethodEnum;
import com.limito.payment.domain.enums.PaymentStatusEnum;

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
public class PaymentDto {

	private UUID paymentId;

	private UUID orderId;

	private PaymentStatusEnum paymentStatus;

	private String paymentKey;

	private String itemSummary;

	private int totalPrice;

	private String refundReason;

	private CancelAndRefundStatusEnum cancelAndRefundStatus;

	private LocalDateTime approvedAt;

	private LocalDateTime refundAt;

	private String failLog;

	private PaymentMethodEnum paymentMethod;

	private String cardNum;

	private String cardName;

	private String pgProvider;

	@Builder.Default
	private List<PaymentItemDto> items = new ArrayList<>();

}
