package com.limito.payment.presentation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.limito.payment.application.PaymentServiceV1;
import com.limito.payment.presentation.dto.request.CreatePaymentRequestV1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/internal/v1/payments")
@RequiredArgsConstructor
public class PaymentInternalControllerV1 {

	private final PaymentServiceV1 paymentService;

	@PostMapping("/{orderId}/confirm")
	public ResponseEntity<Object> confirmPayment(
		@PathVariable String orderId,
		@RequestBody CreatePaymentRequestV1 request) {
		try {
			paymentService.validatePaymentRequestUniqueness(UUID.fromString(orderId));
		} catch (Exception e) {
			log.warn("주문 아이디 orderId={}에 대한 결제 요청 내역이 있습니다. {}", orderId, e.getMessage());
			return ResponseEntity.status(HttpStatus.CONFLICT).body("중복 결제 불가");
		}
		paymentService.createPayment(UUID.fromString(orderId), request);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
