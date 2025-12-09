package com.limito.payment.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelAndRefundStatusEnum {
	REQUESTED("결제 취소/환불 요청"),
	CANCEL("결제 취소 완료"),
	REFUND("결제 환불 완료"),
	REJECTED("결제 취소/환불 거절"),
	FAILED("결제 취소/환불 실패");
	private final String description;
}