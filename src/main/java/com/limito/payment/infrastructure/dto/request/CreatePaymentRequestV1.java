package com.limito.payment.infrastructure.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NotBlank
public class CreatePaymentRequestV1 {
	private Long orderId;
	@NotBlank(message = "상품 요약은 필수입니다.")
	private String itemSummary;
	@Positive(message = "총 결제 금액은 양수여야 합니다.")
	@NotNull(message = "총 결제 금액은 필수입니다.")
	private int totalPrice;
	@Size(min = 1, message = "결제 상품은 최소 1개 이상이어야 합니다.")
	private List<OrderItem> items;

}
