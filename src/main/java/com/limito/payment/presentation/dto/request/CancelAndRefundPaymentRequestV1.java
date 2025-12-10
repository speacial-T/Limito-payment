package com.limito.payment.presentation.dto.request;

import java.util.UUID;

import com.limito.payment.domain.enums.CancelAndRefundStatusEnum;

import lombok.Getter;

@Getter
public class CancelAndRefundPaymentRequestV1 {
	String refundReason;
	CancelAndRefundStatusEnum cancelType;
}
