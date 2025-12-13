package com.limito.payment.presentation.dto;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class FailLogPaymentResponseV1 {
	private String code;
	private String message;
	private String paymentId;
}