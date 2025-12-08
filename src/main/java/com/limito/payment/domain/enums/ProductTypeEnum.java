package com.limito.payment.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductTypeEnum {
	LIMITED("한정판매"), RESELL("리셀");

	private final String type;
}