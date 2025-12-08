package com.limito.payment.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatusEnum {
	IN_PROGRESS("결제 요청 진행중"),
	SUCCESS("결제 완료"),
	CANCELED("결제 취소"),
	REFUND("환불"),
	FAILED("결제 실패");
	private final String description;
}