package com.limito.payment.domain.exception;

import org.springframework.http.HttpStatus;

import com.limito.common.code.ErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PaymentErrorCode implements ErrorCode {
	PAYMENT_DUPLICATE_ORDER(HttpStatus.BAD_REQUEST, "중복된 결제내역이 존재합니다."),
	PAYMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 주문 번호의 결제 내역을 찾지 못했습니다."),
	PAYMENT_TOTAL_PRICE_ERROR(HttpStatus.BAD_REQUEST, "총 결제 금액이 주문 상품 금액의 총합과 일치하지 않습니다."),
	PAYMENT_CAN_NOT_CONFIRM(HttpStatus.BAD_REQUEST, "결제 확인이 불가능한 상태입니다."),
	PAYMENT_IS_NOT_SUCCESS(HttpStatus.BAD_REQUEST, "결제 완료 상태가 아닙니다."),
	PAYMENT_CAN_NOT_REFUND(HttpStatus.BAD_REQUEST, "이미 환불된 결제입니다"),
	PAYMENT_REFUND_FAILED(HttpStatus.BAD_REQUEST, "환불에 실패했습니다."),
	PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "구매한 유저/상품 판매자가 아닙니다.");

	private final HttpStatus status;
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
