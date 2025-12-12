package com.limito.payment.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethodEnum {
	CARD("카드결제"),
	EASY_PAY_N_PAY("간편결제-네이버페이"),
	EASY_PAY_K_PAY("간편결제-카카오페이"),
	EASY_PAY_T_PAY("간편결제-토스페이");

	private final String description;
}
