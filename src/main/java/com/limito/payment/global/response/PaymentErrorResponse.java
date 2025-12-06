package com.limito.payment.global.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record PaymentErrorResponse(String errorCode, String message) {

	public static ResponseEntity<PaymentErrorResponse> paymentErrorResponse(
		HttpStatus status, String errorCode, String message
	) {
		return ResponseEntity
			.status(status)
			.body(new PaymentErrorResponse(errorCode, message));
	}
}