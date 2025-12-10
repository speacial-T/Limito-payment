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
import com.limito.payment.domain.enums.CancelAndRefundStatusEnum;
import com.limito.payment.infrastructure.dto.request.CreatePaymentRequestV1;
import com.limito.payment.presentation.dto.request.CancelAndRefundPaymentRequestV1;

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
			paymentService.validPaymentRequest(UUID.fromString(orderId), request);
		} catch (Exception e) {
			log.warn("주문 아이디 orderId={}에 대한 결제 요청 내역이 있습니다. {}", orderId, e.getMessage());
			return ResponseEntity.status(HttpStatus.CONFLICT).body("해당 중복 결제 불가");
		}
		paymentService.createPayment(UUID.fromString(orderId), request);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<Object> cancelPayment(
		@PathVariable String orderId,
		@RequestBody CancelAndRefundPaymentRequestV1 request) {
		try {
			paymentService.validPaymentCancelOrRefundRequest(UUID.fromString(orderId));
		} catch (Exception e) {
			log.warn("주문 아이디 orderId={}에 대해 {}을 이유로 결제 환불 내역이 있습니다", orderId, request.getRefundReason(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(String.format("주문 아이디 %s에 대해 결제 취소를 진행할 수 없습니다.", orderId));
		}
		paymentService.cancelAndRefundPayment(UUID.fromString(orderId), CancelAndRefundStatusEnum.CANCEL,
			request.getRefundReason());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PostMapping("/{orderId}/refund")
	public ResponseEntity<Object> refundPayment(
		@PathVariable String orderId,
		@RequestBody CancelAndRefundPaymentRequestV1 request) {
		try {
			paymentService.validPaymentCancelOrRefundRequest(UUID.fromString(orderId));
		} catch (Exception e) {
			log.warn("주문 아이디 orderId={}에 대해 {}을 이유로 결제 환불을 진행할 수 없습니다", orderId, request.getRefundReason(), e);
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(String.format("주문 아이디 %s에 대해 결제 환불을 진행할 수 없습니다.", orderId));
		}
		paymentService.cancelAndRefundPayment(UUID.fromString(orderId), CancelAndRefundStatusEnum.REFUND,
			request.getRefundReason());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
