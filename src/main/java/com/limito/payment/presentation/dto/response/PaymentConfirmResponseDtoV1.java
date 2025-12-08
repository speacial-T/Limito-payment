package com.limito.payment.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.limito.payment.domain.enums.PaymentMethodEnum;
import com.limito.payment.domain.enums.PaymentStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PaymentConfirmResponseDtoV1 {
	private UUID orderId;
	private PaymentStatusEnum paymentStatus; // SUCCESS / FAILED
	private PaymentMethodEnum paymentMethod;
	private LocalDateTime approvedAt;        // 승인 시각
}
