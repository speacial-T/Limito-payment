package com.limito.payment.infrastructure.client.portone.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.domain.enums.CancelAndRefundStatusEnum;
import com.limito.payment.domain.enums.PaymentMethodEnum;
import com.limito.payment.domain.enums.PaymentStatusEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOnePaymentMapper {

	private final ObjectMapper objectMapper;

	public PaymentDetailDtoV1 extractExtraInfo(String json) {
		try {
			JsonNode root = objectMapper.readTree(json);
			PaymentMethodEnum method = null;
			String status = root.path("status").asText(null);
			String paymentKey = root.path("id").asText(null);
			String failLog = null;
			JsonNode failureNode = root.path("failure");
			// TODO: refactor - if/else
			if (!failureNode.isMissingNode() && !failureNode.isNull()) {
				failLog = failureNode.path("reason").asText(null);
			}

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
					log.info("method=EASY_PAY, easyPayProvider={}", easyPayProvider);

				}
				default -> {
				}
			}
			if (easyPayProvider != null) {
				switch (easyPayProvider) {
					case "KAKAOPAY" -> {
						method = PaymentMethodEnum.EASY_PAY_K_PAY;
					}
					case "TOSSPAY" -> {
						method = PaymentMethodEnum.EASY_PAY_T_PAY;
					}
					case "NAVERPAY" -> {
						method = PaymentMethodEnum.EASY_PAY_N_PAY;
					}
					default -> {
					}
				}
				log.info("mapped easyPayProvider={} to method={}", easyPayProvider, method);
			}
			return PaymentDetailDtoV1.builder()
				.paymentStatus(convertConfirmStatus(status))
				.paymentKey(paymentKey)
				.cardName(cardName)
				.cardNum(cardNum)
				.pgProvider(pgProvider)
				.paymentMethod(method)
				.failLog(failLog)
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
					.build();
			}

			// 실패한 경우 — failLog 추출
			String failLog = extractFailLog(root);

			return PaymentDetailDtoV1.builder()
				.cancelAndRefundStatus(CancelAndRefundStatusEnum.FAILED)
				.failLog(failLog)
				.build();

		} catch (Exception e) {
			log.error("Failed to parse PortOne cancel JSON", e);

			return PaymentDetailDtoV1.builder()
				.cancelAndRefundStatus(CancelAndRefundStatusEnum.FAILED)
				.failLog("PARSE_ERROR: " + e.getMessage())
				.build();
		}
	}

	private PaymentStatusEnum convertConfirmStatus(String status) {
		if (status == null)
			return null;
		// TODO: refactor - switch
		return switch (status.toUpperCase()) {
			case "READY" -> PaymentStatusEnum.IN_PROGRESS;
			case "PAID" -> PaymentStatusEnum.SUCCESS;
			case "CANCELLED" -> PaymentStatusEnum.CANCELED;
			case "FAILED" -> PaymentStatusEnum.FAILED;
			default -> null;
		};
	}

	private String extractFailLog(JsonNode root) {

		String type = root.path("type").asText(null);
		String message = root.path("message").asText(null);
		String code = root.path("code").asText(null);
		String status = root.path("status").asText(null);
		String reason = root.path("reason").asText(null);

		StringBuilder sb = new StringBuilder();

		if (type != null)
			sb.append("type=").append(type).append(" ");
		if (message != null)
			sb.append("message=").append(message).append(" ");
		if (code != null)
			sb.append("code=").append(code).append(" ");
		if (status != null)
			sb.append("status=").append(status).append(" ");
		if (reason != null)
			sb.append("reason=").append(reason).append(" ");

		if (sb.length() == 0) {
			sb.append("UNKNOWN_FAIL_RESPONSE: ").append(root.toString());
		}

		return sb.toString().trim();
	}
}
