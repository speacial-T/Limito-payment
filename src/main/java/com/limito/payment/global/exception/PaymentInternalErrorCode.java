package com.limito.payment.global.exception;

import org.springframework.http.HttpStatus;

import com.limito.common.code.ErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PaymentInternalErrorCode implements ErrorCode {
	PAYMENT_DUPLICATE_ORDER(HttpStatus.BAD_REQUEST, "E001", "중복된 결제내역이 존재합니다.");

	private final HttpStatus status;
	@Getter
	private final String errorCode;
	private final String message;

	@Override
	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
