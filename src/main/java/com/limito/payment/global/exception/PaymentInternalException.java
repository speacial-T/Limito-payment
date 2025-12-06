package com.limito.payment.global.exception;

import org.springframework.http.HttpStatus;

import com.limito.payment.global.response.PaymentErrorResponse;

import lombok.Generated;

public class PaymentInternalException extends RuntimeException {

	private final HttpStatus status;
	private final String errorCode;

	public PaymentInternalException(PaymentErrorResponse errorCode) {
		super(errorCode.getMessage());
		this.status = errorCode.getStatus();
		this.errorCode = errorCode.getErrorCode();
	}

	public static PaymentInternalException of(PaymentErrorResponse errorCode) {
		return new PaymentInternalException(errorCode);
	}

	@Generated
	public HttpStatus getStatus() {
		return this.status;
	}

	@Generated
	public String getErrorCode() {
		return this.errorCode;
	}
}
