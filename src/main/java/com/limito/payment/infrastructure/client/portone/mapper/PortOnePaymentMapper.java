package com.limito.payment.infrastructure.client.portone.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.domain.dto.PaymentLogDetailDtoV1;
import com.limito.payment.domain.enums.PaymentMethodEnum;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;
import com.limito.payment.domain.model.PaymentLogMapper;
import com.limito.payment.presentation.dto.response.FailLogPaymentResponseV1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOnePaymentMapper {

	private final ObjectMapper objectMapper;
	private final PaymentLogMapper paymentLogMapper;

	public PaymentDetailDtoV1 extractExtraInfo(String json) {
		try {
			JsonNode root = objectMapper.readTree(json);
			PaymentMethodEnum method = null;
			String status = root.path("status").asText(null);
			String paymentKey = root.path("id").asText(null);
			LocalDateTime approvedAt = null;
			JsonNode paidAtNode = root.path("paidAt");
			// TODO: refactor - if/else
			if (!paidAtNode.isMissingNode() && !paidAtNode.isNull()) {
				approvedAt = ZonedDateTime.parse(paidAtNode.asText()).toLocalDateTime();
			}
			String cardName = null;
			String cardNum = null;
			String pgProvider = null;
			String easyPayProvider = null;

			String methodType = root.path("method").path("type").asText(null);
			log.info("PortOnePaymentMapper.extractExtraInfo methodType={}", methodType);
			switch (methodType) {
				case "PaymentMethodCard" -> {
					method = PaymentMethodEnum.CARD;
					cardName = root.path("method")
						.path("card")
						.path("name")
						.asText(null);
					cardNum = root.path("method")
						.path("card")
						.path("number")
						.asText(null);
					pgProvider = root.path("channel")
						.path("pgProvider")
						.asText(null);
					log.info("method=CARD, cardName={}, cardNum={}, pgProvider={}", cardName, cardNum, pgProvider);

				}
				case "PaymentMethodEasyPay" -> {
					easyPayProvider = root.path("method").path("provider").asText(null);
					pgProvider = root.path("channel")
						.path("pgProvider")
						.asText(null);
					log.info("method=EASY_PAY, easyPayProvider={}", easyPayProvider);

				}
				default -> {
				}
			}
			if (easyPayProvider != null) {
				switch (easyPayProvider) {
					case "KAKAOPAY" -> method = PaymentMethodEnum.EASY_PAY_K_PAY;
					case "TOSSPAY" -> method = PaymentMethodEnum.EASY_PAY_T_PAY;
					case "NAVERPAY" -> method = PaymentMethodEnum.EASY_PAY_N_PAY;
					default -> {
					}
				}
				log.info("mapped easyPayProvider={} to method={}", easyPayProvider, method);
			}

			List<PaymentLogDetailDtoV1> logs =
				List.of(paymentLogMapper.mapFailLogToPaymentConfirmLog(
					extractFailLog(root)
				));

			return PaymentDetailDtoV1.builder()
				.paymentStatus(convertConfirmStatus(status))
				.paymentKey(paymentKey)
				.cardName(cardName)
				.cardNum(cardNum)
				.pgProvider(pgProvider)
				.paymentMethod(method)
				.logs(logs)
				.approvedAt(approvedAt)
				.build();

		} catch (Exception e) {
			log.error("Failed to parse PortOne JSON", e);
			throw new RuntimeException(e);
		}
	}

	public PaymentDetailDtoV1 extractCancelInfo(String json) {
		try {
			JsonNode root = objectMapper.readTree(json);

			JsonNode cancellationNode = root.path("cancellation");
			String cancelledAtText = cancellationNode.path("cancelledAt").asText(null);

			// 정상적으로 취소된 경우
			if (cancelledAtText != null) {
				return PaymentDetailDtoV1.builder()
					.refundAt(Instant.parse(cancelledAtText)
						.atZone(ZoneId.of("Asia/Seoul"))
						.toLocalDateTime())
					.refundStatus(RefundStatusEnum.REFUND)
					.paymentStatus(PaymentStatusEnum.REFUND)
					.build();
			}

			// 실패한 경우 — failLog 추출
			// String failLog = extractFailLog(root);

			return PaymentDetailDtoV1.builder()
				.refundStatus(RefundStatusEnum.FAILED)
				.build();

		} catch (Exception e) {
			log.error("Failed to parse PortOne cancel JSON", e);

			return PaymentDetailDtoV1.builder()
				.refundStatus(RefundStatusEnum.FAILED)
				.build();
		}
	}

	private PaymentStatusEnum convertConfirmStatus(String status) {
		if (status == null) {
			return null;
		}
		// TODO: refactor - switch
		return switch (status.toUpperCase()) {
			case "READY" -> PaymentStatusEnum.IN_PROGRESS;
			case "PAID" -> PaymentStatusEnum.SUCCESS;
			case "CANCELLED" -> PaymentStatusEnum.REFUND;
			case "FAILED" -> PaymentStatusEnum.FAILED;
			default -> null;
		};
	}

	private FailLogPaymentResponseV1 extractFailLog(JsonNode itemNode) {

		FailLogPaymentResponseV1.FailLogPaymentResponseV1Builder builder =
			FailLogPaymentResponseV1.builder()
				.paymentId(itemNode.path("paymentId").asText())
				.pgProvider(itemNode.path("channel").path("pgProvider").asText(null))
				.responsePayload(itemNode.toString());

		// PG 레벨 실패
		JsonNode failureNode = itemNode.get("failure");
		if (failureNode != null && !failureNode.isNull()) {
			return builder
				.paymentStatus(PaymentStatusEnum.FAILED)
				.refundStatus(RefundStatusEnum.NOT_REQUESTED)
				.failureReason(failureNode.path("reason").asText())
				.pgErrorCode(failureNode.path("pgCode").asText())
				.pgErrorMessage(failureNode.path("pgMessage").asText())
				.build();
		}

		// PG 승인 성공 (→ 이후 서비스 실패 가능 상태)
		return builder
			.paymentStatus(PaymentStatusEnum.SUCCESS)
			.refundStatus(RefundStatusEnum.NOT_REQUESTED)
			.pgTransactionId(itemNode.path("pgTxId").asText(null))
			.failureReason(null)
			.build();
	}
}
