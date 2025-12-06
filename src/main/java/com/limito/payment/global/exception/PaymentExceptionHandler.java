package com.limito.payment.global.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.limito.payment.global.response.PaymentErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class PaymentExceptionHandler {
	@ExceptionHandler(PaymentInternalException.class)
	public ResponseEntity<PaymentErrorResponse> handleAppException(PaymentInternalException exception) {
		log.error("[PaymentInternalException] status={} errorCode={} message={}",
			exception.getStatus(), exception.getErrorCode(), exception.getMessage(), exception);

		// AppException은 status + message만 사용
		return PaymentErrorResponse.paymentErrorResponse(
			exception.getStatus(), exception.getErrorCode(), exception.getMessage()
		);
	}
}
