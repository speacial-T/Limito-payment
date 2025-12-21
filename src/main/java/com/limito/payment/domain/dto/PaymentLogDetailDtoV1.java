package com.limito.payment.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

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
@ToString
public class PaymentLogDetailDtoV1 {
	UUID paymentLogId;
	UUID paymentId;
	PaymentStatusEnum paymentStatus;
	RefundStatusEnum refundStatus;
	String paymentKey;
	String pgTransactionId;
	String pgProvider;
	@Setter
	String idempotencyKey;
	@Setter
	int tryCount;
	String failureReason;
	String pgErrorCode;
	String pgErrorMessage;
	String apiEndpoint;
	String requestPayload;
	String responsePayload;
	LocalDateTime createdAt;

}
