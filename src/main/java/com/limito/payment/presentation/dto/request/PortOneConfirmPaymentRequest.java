package com.limito.payment.presentation.dto.request;

import java.util.List;
import java.util.UUID;

import com.limito.payment.infrastructure.dto.request.OrderItem;

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
//포트원에 결제 요청 보내는 용
public class PortOneConfirmPaymentRequest {
	private UUID orderId;

	private String itemSummary;

	private List<OrderItem> items;

	private int totalPrice;
}
