package com.limito.payment.presentation.dto.response;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailLogPaymentResponseV1 {
	private String paymentId;
	private PaymentStatusEnum paymentStatus;
	private RefundStatusEnum refundStatus;
	private String pgTransactionId;
	private String pgProvider;
	private int retryCount;
	private String failureReason;
	private String pgErrorCode;
	private String pgErrorMessage;
	private Integer httpStatus;
	private String apiEndpoint;
	private String requestPayload;
	private String responsePayload;
}
