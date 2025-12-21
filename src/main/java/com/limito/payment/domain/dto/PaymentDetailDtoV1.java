package com.limito.payment.domain.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.limito.payment.domain.enums.PaymentMethodEnum;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;

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
public class PaymentDetailDtoV1 {

	private UUID paymentId;

	private Long userId;

	private UUID orderId;

	private PaymentStatusEnum paymentStatus;

	private String paymentKey;

	private String itemSummary;

	private int totalPrice;

	private String refundReason;

	private RefundStatusEnum refundStatus;

	private LocalDateTime approvedAt;

	private LocalDateTime refundAt;

	private PaymentMethodEnum paymentMethod;

	private String cardNum;

	private String cardName;

	private String pgProvider;

	@Builder.Default
	private List<PaymentItemDetailDtoV1> items = new ArrayList<>();

	@Builder.Default
	private List<PaymentLogDetailDtoV1> logs = new ArrayList<>();
}
