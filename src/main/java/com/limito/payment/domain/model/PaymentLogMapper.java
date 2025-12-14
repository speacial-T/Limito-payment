package com.limito.payment.domain.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.limito.payment.domain.dto.PaymentLogDetailDtoV1;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentLogMapper {
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
			.paymentLogId(dto.getPaymentLogId())
			.payment(null)
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

	public List<PaymentLogEntity> create(PaymentEntity payment) {
		PaymentLogEntity log = PaymentLogEntity.builder()
			.paymentStatus(PaymentStatusEnum.IN_PROGRESS)
			.refundStatus(RefundStatusEnum.NOT_REQUESTED)
			.build();
		log.assignPayment(payment);
		List<PaymentLogEntity> paymentLogEntities = new ArrayList<PaymentLogEntity>();
		paymentLogEntities.add(log);
		return paymentLogEntities;
	}
}
