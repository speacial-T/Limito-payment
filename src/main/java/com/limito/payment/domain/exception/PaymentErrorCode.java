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
	PAYMENT_CAN_NOT_CONFIRM(HttpStatus.BAD_REQUEST, "E004", "결제 확인이 불가능한 상태입니다."),
	PAYMENT_CAN_NOT_CANCEL_OR_REFUND(HttpStatus.BAD_REQUEST, "E004", "결제 완료가 아닌 결제는 결제취소/환불 할 수 없습니다");
	@Getter
	private final HttpStatus status;
	@Getter
	private final String errorCode;
	@Getter
	private final String message;
}
