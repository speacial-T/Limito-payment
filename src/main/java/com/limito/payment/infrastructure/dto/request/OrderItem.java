package com.limito.payment.infrastructure.dto.request;

import java.util.UUID;

import com.limito.payment.domain.enums.ProductTypeEnum;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {

	@NotNull(message = "상품의 옵션 아이디는 필수입니다.")
	private UUID optionId;

	@NotNull(message = "상품명은 필수입니다.")
	private String productName;

	@Positive(message = "상품 가격은 양수여야 합니다.")
	@NotNull(message = "상품 가격은 필수입니다.")
	private int productPrice;

	@NotNull(message = "해당 상품의 구매 수량을 넣어주세요.")
	@Size(min = 1, message = "결제 상품은 최소 1개 이상이어야 합니다.")
	private int quantity;

	@NotNull(message = "상품 판매자 정보은 필수입니다.")
	private Long sellerId;

	@NotNull(message = "상품 타입은 필수입니다.")
	private ProductTypeEnum productType;
}