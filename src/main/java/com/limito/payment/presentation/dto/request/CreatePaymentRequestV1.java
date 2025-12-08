package com.limito.payment.presentation.dto.request;

import java.util.List;

import com.limito.payment.infrastructure.dto.request.OrderItem;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CreatePaymentRequestV1 {
	@NotNull(message = "주문 요약은 필수입니다.")
	private String itemSummary;

	@NotNull(message = "결제를 진행할 상품 목록은 필수입니다.")
	private List<OrderItem> items;

	@NotNull(message = "총 결제 금액은 필수입니다.")
	private int totalPrice;
}
