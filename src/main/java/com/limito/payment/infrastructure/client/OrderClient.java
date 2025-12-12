package com.limito.payment.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.limito.payment.presentation.dto.response.PaymentRefundResponseDtoV1;

@FeignClient(name = "order-service", url = "${feign.order-service.url}")
public interface OrderClient {
	@PostMapping("/internal/v1/orders/success-payments/limited/{orderId}")
	ResponseEntity<Void> notifyPaymentLimitedSuccess(@PathVariable("orderId") UUID orderId);

	@PostMapping("/internal/v1/orders/success-payments/resell/{orderId}")
	ResponseEntity<Void> notifyPaymentResellSuccess(@PathVariable("orderId") UUID orderId);

	@PostMapping("/internal/v1/orders/fail-payments/{orderId}")
	ResponseEntity<Void> notifyPaymentFail(@PathVariable("orderId") UUID orderId);

	@PatchMapping("/internal/v1/orders/{orderId}/cancel/limited")
	ResponseEntity<Void> notifyPaymentRefundLimitedSuccess(@PathVariable("orderId") UUID orderId,
		@RequestBody PaymentRefundResponseDtoV1 refundResponseDto);

	@PatchMapping("/internal/v1/orders/{orderId}/cancel/resell")
	ResponseEntity<Void> notifyPaymentRefundResellSuccess(@PathVariable("orderId") UUID orderId,
		@RequestBody PaymentRefundResponseDtoV1 refundResponseDto);
}
