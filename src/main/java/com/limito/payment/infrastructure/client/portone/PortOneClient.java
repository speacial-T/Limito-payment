package com.limito.payment.infrastructure.client.portone;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortOneClient {

	private final WebClient portOneWebClient;

	@Value("${portone.store-id}")
	private String storeId;

	public String getPaymentRawPaymentInfoJson(String paymentId) {

		String response = portOneWebClient.get()
			.uri(uriBuilder -> {
				URI uri = uriBuilder
					.path("/payments/{paymentId}")
					.queryParam("storeId", storeId.trim())
					.build(paymentId);
				return uri;
			})
			.retrieve()
			.bodyToMono(String.class)
			.onErrorResume(e -> {
				log.error("PortOne 결제 조회 실패 paymentId={}, message={}", paymentId, e.getMessage());
				return Mono.error(e);
			})
			.block();

		return response;
	}

	public String cancelPayment(String paymentId, String reason) {

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("storeId", storeId.trim());
		requestBody.put("reason", reason);

		String response = portOneWebClient.post()
			.uri(uriBuilder -> {
				URI uri = uriBuilder
					.path("/payments/{paymentId}/cancel")
					.build(paymentId);
				return uri;
			})
			.bodyValue(requestBody)
			.retrieve()
			.bodyToMono(String.class)
			.onErrorResume(e -> {
				log.error("PortOne 결제 취소 실패 paymentId={}, message={}", paymentId, e.getMessage());
				return Mono.error(e);
			})
			.block();

		return response;
	}
}