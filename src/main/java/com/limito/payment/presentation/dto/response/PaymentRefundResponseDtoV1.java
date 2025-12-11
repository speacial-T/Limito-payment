package com.limito.payment.presentation.dto.response;

import com.limito.payment.domain.enums.PaymentStatusEnum;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PaymentRefundResponseDtoV1 {
	PaymentStatusEnum paymentStatusEnum;
}
