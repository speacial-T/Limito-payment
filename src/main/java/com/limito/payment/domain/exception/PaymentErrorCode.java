package com.limito.payment.domain.exception;

import org.springframework.http.HttpStatus;

import com.limito.common.code.ErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PaymentErrorCode implements ErrorCode {
	PAYMENT_DUPLICATE_ORDER(HttpStatus.BAD_REQUEST, "E001", "중복된 결제내역이 존재합니다."),
	PAYMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "E003", "해당 주문 번호의 결제 내역을 찾지 못했습니다."),
	PAYMENT_TOTAL_PRICE_ERROR(HttpStatus.BAD_REQUEST, "E002", "총 결제 금액이 주문 상품 금액의 총합과 일치하지 않습니다."),
	PAYMENT_VALIDATE_ERROR(HttpStatus.BAD_REQUEST, "E004", "진행중인 결제만 승인 가능합니다");

	@Getter
	private final HttpStatus status;
	@Getter
	private final String errorCode;
	@Getter
	private final String message;
}
