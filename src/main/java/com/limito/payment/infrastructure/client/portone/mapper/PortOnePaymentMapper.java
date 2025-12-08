package com.limito.payment.infrastructure.client.portone.mapper;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limito.payment.domain.dto.PaymentDetailDtoV1;
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

			String status = root.path("status").asText(null);
			String paymentKey = root.path("id").asText(null);

			String cardName = root.path("method").path("card").path("name").asText(null);
			String pgProvider = root.path("channel").path("pgProvider").asText(null);

			String failLog = null;
			JsonNode failureNode = root.path("failure");
			if (!failureNode.isMissingNode() && !failureNode.isNull()) {
				failLog = failureNode.path("reason").asText(null);
			}

			LocalDateTime approvedAt = null;
			JsonNode paidAtNode = root.path("paidAt");
			if (!paidAtNode.isMissingNode() && !paidAtNode.isNull()) {
				approvedAt = ZonedDateTime.parse(paidAtNode.asText()).toLocalDateTime();
			}

			return PaymentDetailDtoV1.builder()
				.paymentStatus(convertStatus(status))
				.paymentKey(paymentKey)
				.cardName(cardName)
				.pgProvider(pgProvider)
				.failLog(failLog)
				.approvedAt(approvedAt)
				.build();

		} catch (Exception e) {
			log.error("Failed to parse PortOne JSON", e);
			throw new RuntimeException(e);
		}
	}

	private PaymentStatusEnum convertStatus(String status) {
		if (status == null)
			return null;
		return switch (status.toUpperCase()) {
			case "READY" -> PaymentStatusEnum.IN_PROGRESS;
			case "PAID" -> PaymentStatusEnum.SUCCESS;
			case "CANCELLED" -> PaymentStatusEnum.CANCELED;
			case "FAILED" -> PaymentStatusEnum.FAILED;
			default -> null;
		};
	}
}
