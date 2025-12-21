package com.limito.payment.domain.model;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.domain.dto.PaymentLogDetailDtoV1;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;
import com.limito.payment.presentation.dto.response.ConfirmPaymentResponseV1;
import com.limito.payment.presentation.dto.response.FailLogPaymentResponseV1;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentLogMapper {

	@Value("${portone.store-id}")
	private String storeId;

	public PaymentLogDetailDtoV1 mapFailLogToPaymentConfirmLog(FailLogPaymentResponseV1 response) {
		return PaymentLogDetailDtoV1.builder()
			.paymentStatus(response.getPaymentStatus())
			.refundStatus(response.getRefundStatus())
			.paymentKey(response.getPaymentId())
			.pgTransactionId(response.getPgTransactionId())
			.pgProvider(response.getPgProvider())
			.tryCount(response.getTryCount())
			.failureReason(response.getFailureReason())
			.pgErrorCode(response.getPgErrorCode())
			.pgErrorMessage(response.getPgErrorMessage())
			.apiEndpoint(response.getApiEndpoint())
			.requestPayload(response.getRequestPayload())
			.responsePayload(response.getResponsePayload())
			.build();
	}

	public PaymentLogDetailDtoV1 mapToPaymentConfirmLog(ConfirmPaymentResponseV1 response) {
		return PaymentLogDetailDtoV1.builder()
			.paymentKey(response.getPaymentKey())
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
			.tryCount(entity.tryCount)
			.failureReason(entity.failureReason)
			.pgErrorCode(entity.pgErrorCode)
			.pgErrorMessage(entity.pgErrorMessage)
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
			.tryCount(dto.getTryCount())
			.failureReason(dto.getFailureReason())
			.pgErrorCode(dto.getPgErrorCode())
			.pgErrorMessage(dto.getPgErrorMessage())
			.apiEndpoint(dto.getApiEndpoint())
			.requestPayload(dto.getRequestPayload())
			.responsePayload(dto.getResponsePayload())
			.createdAt(dto.getCreatedAt())
			.build();
	}

	public PaymentLogDetailDtoV1 createConfirmLog(PaymentEntity payment) {
		return PaymentLogDetailDtoV1.builder()
			.paymentId(payment.paymentId)
			.paymentKey(null)
			.paymentStatus(PaymentStatusEnum.IN_PROGRESS)
			.refundStatus(RefundStatusEnum.REQUESTED)
			.idempotencyKey(UUID.randomUUID().toString())
			.apiEndpoint(String.format("/pass/%s/page", payment.orderId))
			.requestPayload(null)
			.responsePayload(null)
			.build();
	}

	public PaymentLogEntity addConfirmLog(PaymentEntity payment, PaymentLogDetailDtoV1 logDetailDto) {
		return PaymentLogEntity.builder()
			.paymentId(payment.paymentId)
			.paymentKey(logDetailDto.getPaymentKey())
			.pgProvider("TOSSPAYMENTS")
			.idempotencyKey(logDetailDto.getIdempotencyKey())
			.tryCount(logDetailDto.getTryCount())
			.apiEndpoint(String.format("/api/v1/payments/pass/%s/confirm", logDetailDto.getPaymentKey()))
			.paymentStatus(payment.paymentStatus)
			.refundStatus(RefundStatusEnum.NOT_REQUESTED)
			.requestPayload(logDetailDto.getRequestPayload())
			.responsePayload(logDetailDto.getResponsePayload())
			.build();
	}

	public PaymentLogEntity addRefundFailLog(PaymentDetailDtoV1 detailDto, FailLogPaymentResponseV1 failLog,
		PaymentLogDetailDtoV1 logDto) {
		return PaymentLogEntity.builder()
			.paymentId(detailDto.getPaymentId())
			.paymentKey(detailDto.getPaymentKey())
			.pgTransactionId(logDto.getPgTransactionId())
			.pgProvider(detailDto.getPgProvider())
			.idempotencyKey(logDto.getIdempotencyKey())
			.tryCount(logDto.getTryCount())
			.apiEndpoint(String.format("https://api.portone.io/payments/%s/cancel", detailDto.getPaymentKey()))
			.pgErrorCode(failLog.getPgErrorCode())
			.pgErrorMessage(failLog.getPgErrorMessage())
			.paymentStatus(detailDto.getPaymentStatus())
			.refundStatus(detailDto.getRefundStatus())
			//todo 보완
			.requestPayload(failLog.getRequestPayload())
			.responsePayload(failLog.getResponsePayload())
			.build();
	}

	public PaymentLogEntity createRefundLog(PaymentEntity payment, String refundReason) {
		return PaymentLogEntity.builder()
			.paymentId(payment.paymentId)
			.paymentKey(payment.paymentKey)
			.paymentStatus(payment.paymentStatus)
			.refundStatus(RefundStatusEnum.REQUESTED)
			.idempotencyKey(UUID.randomUUID().toString())
			.apiEndpoint(String.format("/%s/refund", payment.orderId))
			.requestPayload(String.format("{ \"refundReason\": \"%s\"}", refundReason))
			.responsePayload(null)
			.build();
	}

	public PaymentLogEntity addRefundLog(PaymentLogEntity paymentLog, PaymentDetailDtoV1 detailDtoV1) {
		return PaymentLogEntity.builder()
			.paymentId(paymentLog.paymentId)
			.paymentKey(paymentLog.paymentKey)
			.paymentStatus(paymentLog.paymentStatus)
			.refundStatus(RefundStatusEnum.REQUESTED)
			.idempotencyKey(paymentLog.idempotencyKey)
			.apiEndpoint(String.format("/payments/%s/cancel", detailDtoV1.getPaymentKey()))
			.requestPayload(
				String.format("{ \"storeId\": \"%s\",\"reason\": \"%s\"}", storeId,
					detailDtoV1.getRefundReason()))
			.responsePayload(detailDtoV1.getLogs().get(0).getResponsePayload())
			.build();
	}

}
