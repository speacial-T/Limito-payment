package com.limito.payment.infrastructure.client.portone;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-service", url = "${feign.order-service.url}")
public interface OrderClient {
	@PostMapping("/internal/v1/orders/success-payments/{orderId}")
	ResponseEntity<Void> notifyPaymentSuccess(@PathVariable("orderId") UUID orderId);

	@PostMapping("/internal/v1/orders/fail-payments/{orderId}")
	ResponseEntity<Void> notifyPaymentFail(@PathVariable("orderId") UUID orderId);
}