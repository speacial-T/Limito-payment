package com.limito.payment.domain.model;

import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.domain.dto.PaymentItemDetailDtoV1;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;
import com.limito.payment.infrastructure.dto.request.CreatePaymentRequestV1;
import com.limito.payment.presentation.dto.response.PaymentConfirmResponseDtoV1;
import com.limito.payment.presentation.dto.response.PaymentRefundResponseDtoV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentMapper {

    private final PaymentItemMapper itemMapper;
    private final PaymentLogMapper logMapper;

    public static PaymentEntity create(UUID orderId, CreatePaymentRequestV1 request, Long userId) {
        return new PaymentEntity(
                null, orderId, PaymentStatusEnum.IN_PROGRESS,
                null, request.getItemSummary(), request.getTotalPrice(),
                null, RefundStatusEnum.NOT_REQUESTED, null, null, null,
                null, null, null, new ArrayList<>(), userId
        );
    }

    public PaymentEntity toEntity(PaymentDetailDtoV1 dto) {
        if (dto == null) {
            return null;
        }
        PaymentEntity entity = PaymentEntity.builder()
                .paymentId(dto.getPaymentId())
                .orderId(dto.getOrderId())
                .paymentStatus(dto.getPaymentStatus())
                .paymentKey(dto.getPaymentKey())
                .itemSummary(dto.getItemSummary())
                .totalPrice(Integer.valueOf(dto.getTotalPrice()))
                .refundReason(dto.getRefundReason())
                .refundStatus(dto.getRefundStatus())
                .approvedAt(dto.getApprovedAt())
                .refundAt(dto.getRefundAt())
                .paymentMethod(dto.getPaymentMethod())
                .cardNum(dto.getCardNum())
                .cardName(dto.getCardName())
                .pgProvider(dto.getPgProvider())
                .items(null) // items는 별도로 매핑
                .build();

        List<PaymentItemEntity> items = dto.getItems() == null ? List.of()
                : dto.getItems().stream()
                .filter(java.util.Objects::nonNull)
                .map(itemMapper::toEntity)
                .collect(Collectors.toList());

        if (!items.isEmpty()) {
            items.forEach(item -> item.assignPayment(entity));
            entity.items.addAll(items);
        }
        return entity;
    }

    public PaymentDetailDtoV1 toDto(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }
        PaymentDetailDtoV1.PaymentDetailDtoV1Builder builder = PaymentDetailDtoV1.builder()
                .paymentId(entity.paymentId)
                .orderId(entity.orderId)
                .userId(entity.userId)
                .paymentStatus(entity.paymentStatus)
                .paymentKey(entity.paymentKey)
                .itemSummary(entity.itemSummary)
                .totalPrice(entity.totalPrice != null ? entity.totalPrice : 0)
                .refundReason(entity.refundReason)
                .refundStatus(entity.refundStatus)
                .approvedAt(entity.approvedAt)
                .refundAt(entity.refundAt)
                .paymentMethod(entity.paymentMethod)
                .cardNum(entity.cardNum)
                .cardName(entity.cardName)
                .pgProvider(entity.pgProvider);

        // items 매핑
        if (entity.items != null && !entity.items.isEmpty()) {
            List<PaymentItemDetailDtoV1> itemDtos =
                    entity.items.stream()
                            .filter(Objects::nonNull)
                            .map(itemMapper::toDto)
                            .collect(Collectors.toList());
            builder.items(itemDtos);
        }

        return builder.build();
    }

    public PaymentConfirmResponseDtoV1 forConfirmResponse(PaymentDetailDtoV1 detailDto) {

        return PaymentConfirmResponseDtoV1.builder()
                .orderId(detailDto.getOrderId())
                .paymentStatus(detailDto.getPaymentStatus())
                .paymentMethod(detailDto.getPaymentMethod())
                .approvedAt(detailDto.getApprovedAt())
                .build();
    }

    public PaymentRefundResponseDtoV1 forRefundResponse(PaymentDetailDtoV1 dtoV1) {
        return new PaymentRefundResponseDtoV1(dtoV1.getPaymentStatus());
    }

}
