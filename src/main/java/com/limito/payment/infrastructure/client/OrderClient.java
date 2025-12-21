package com.limito.payment.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.limito.payment.presentation.dto.response.PaymentRefundResponseDtoV1;

@FeignClient(name = "order-service", url = "${feign.order-service.url}")
public interface OrderClient {

	// 한정판매 주문 완료

	@PostMapping("/internal/v1/orders/success-payments/limited/{orderId}")
	ResponseEntity<Void> notifyPaymentLimitedSuccess(@PathVariable("orderId") UUID orderId);

	// 리셀 주문 완료
	@PostMapping("/internal/v1/orders/success-payments/resell/{orderId}")
	ResponseEntity<Void> notifyPaymentResellSuccess(@PathVariable("orderId") UUID orderId);

	// 한정판매 결제 실패(취소) 요청
	@DeleteMapping("/internal/v1/orders/fail-payments/limited/{orderId}")
	ResponseEntity<Void> notifyLimitedPaymentFail(@PathVariable("orderId") UUID orderId);

	// 리셀 결제 실패(취소) 요청
	@DeleteMapping("/internal/v1/orders/fail-payments/resell/{orderId}")
	ResponseEntity<Void> notifyResellPaymentFail(@PathVariable("orderId") UUID orderId);

	@PatchMapping("/internal/v1/orders/{orderId}/cancel/limited")
	ResponseEntity<Void> notifyPaymentRefundLimitedSuccess(@PathVariable("orderId") UUID orderId,
		@RequestBody PaymentRefundResponseDtoV1 refundResponseDto);

	@PatchMapping("/internal/v1/orders/{orderId}/cancel/resell")
	ResponseEntity<Void> notifyPaymentRefundResellSuccess(@PathVariable("orderId") UUID orderId,
		@RequestBody PaymentRefundResponseDtoV1 refundResponseDto);
}
