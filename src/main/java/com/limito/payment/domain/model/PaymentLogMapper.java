package com.limito.payment.domain.model;

import org.springframework.stereotype.Component;

import com.limito.payment.domain.dto.PaymentLogDetailDtoV1;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;
import com.limito.payment.presentation.dto.response.FailLogPaymentResponseV1;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentLogMapper {
	public PaymentLogDetailDtoV1 mapToPaymentConfirmLog(FailLogPaymentResponseV1 response) {
		return PaymentLogDetailDtoV1.builder()
			.paymentStatus(response.getPaymentStatus())
			.refundStatus(response.getRefundStatus())
			.paymentKey(response.getPaymentId())
			.pgTransactionId(response.getPgTransactionId())
			.pgProvider(response.getPgProvider())
			.retryCount(response.getRetryCount())
			.failureReason(response.getFailureReason())
			.pgErrorCode(response.getPgErrorCode())
			.pgErrorMessage(response.getPgErrorMessage())
			.apiEndpoint(response.getApiEndpoint())
			.requestPayload(response.getRequestPayload())
			.responsePayload(response.getResponsePayload())
			.build();
	}

	public PaymentLogDetailDtoV1 toDto(PaymentLogEntity entity) {
		if (entity == null) {
			return null;
		}

		return PaymentLogDetailDtoV1.builder()
			.paymentLogId(entity.paymentLogId)
			.paymentStatus(entity.paymentStatus)
			.refundStatus(entity.refundStatus)
			.paymentKey(entity.paymentKey)
			.pgTransactionId(entity.pgTransactionId)
			.pgProvider(entity.pgProvider)
			.idempotencyKey(entity.idempotencyKey)
			.retryCount(entity.retryCount)
			.failureReason(entity.failureReason)
			.pgErrorCode(entity.pgErrorCode)
			.pgErrorMessage(entity.pgErrorMessage)
			.httpStatus(entity.httpStatus)
			.apiEndpoint(entity.apiEndpoint)
			.requestPayload(entity.requestPayload)
			.responsePayload(entity.responsePayload)
			.createdAt(entity.createdAt)
			.build();
	}

	public PaymentLogEntity toEntity(PaymentLogDetailDtoV1 dto) {
		if (dto == null) {
			return null;
		}

		return PaymentLogEntity.builder()
			.paymentStatus(dto.getPaymentStatus())
			.refundStatus(dto.getRefundStatus())
			.paymentKey(dto.getPaymentKey())
			.pgTransactionId(dto.getPgTransactionId())
			.pgProvider(dto.getPgProvider())
			.idempotencyKey(dto.getIdempotencyKey())
			.retryCount(dto.getRetryCount())
			.failureReason(dto.getFailureReason())
			.pgErrorCode(dto.getPgErrorCode())
			.pgErrorMessage(dto.getPgErrorMessage())
			.httpStatus(dto.getHttpStatus())
			.apiEndpoint(dto.getApiEndpoint())
			.requestPayload(dto.getRequestPayload())
			.responsePayload(dto.getResponsePayload())
			.createdAt(dto.getCreatedAt())
			.build();
	}

	public PaymentLogEntity create(PaymentEntity payment) {
		PaymentLogEntity log = PaymentLogEntity.builder()
			.paymentStatus(PaymentStatusEnum.IN_PROGRESS)
			.refundStatus(RefundStatusEnum.NOT_REQUESTED)
			.build();
		return log;
	}
}
