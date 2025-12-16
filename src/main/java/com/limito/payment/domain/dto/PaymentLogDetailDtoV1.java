package com.limito.payment.domain.dto;

import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
public class PaymentLogDetailDtoV1 {
    UUID paymentLogId;
    PaymentStatusEnum paymentStatus;
    RefundStatusEnum refundStatus;
    String paymentKey;
    String pgTransactionId;
    String pgProvider;
    String idempotencyKey;
    int retryCount;
    String failureReason;
    String pgErrorCode;
    String pgErrorMessage;
    Integer httpStatus;
    String apiEndpoint;
    String requestPayload;
    String responsePayload;
    LocalDateTime createdAt;
}
