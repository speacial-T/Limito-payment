package com.limito.payment.domain.model;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.limito.payment.domain.dto.PaymentDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentMapper {

	private final PaymentItemMapper itemMapper;

	public PaymentEntity toEntity(PaymentDto dto) {
		if (dto == null)
			return null;

		PaymentEntity entity = PaymentEntity.builder()
			.paymentId(dto.getPaymentId())
			.orderId(dto.getOrderId())
			.paymentStatus(dto.getPaymentStatus())
			.paymentKey(dto.getPaymentKey())
			.itemSummary(dto.getItemSummary())
			.totalPrice(Integer.valueOf(dto.getTotalPrice()))
			.refundReason(dto.getRefundReason())
			.cancelAndRefundStatus(dto.getCancelAndRefundStatus())
			.approvedAt(dto.getApprovedAt())
			.refundAt(dto.getRefundAt())
			.failLog(dto.getFailLog())
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

	public PaymentDto toDto(PaymentEntity entity) {
		if (entity == null)
			return null;

		PaymentDto.PaymentDtoBuilder builder = PaymentDto.builder()
			.paymentId(entity.paymentId)
			.orderId(entity.orderId)
			.paymentStatus(entity.paymentStatus)
			.paymentKey(entity.paymentKey)
			.itemSummary(entity.itemSummary)
			.totalPrice(entity.totalPrice != null ? entity.totalPrice : 0)
			.refundReason(entity.refundReason)
			.cancelAndRefundStatus(entity.cancelAndRefundStatus)
			.approvedAt(entity.approvedAt)
			.refundAt(entity.refundAt)
			.failLog(entity.failLog)
			.paymentMethod(entity.paymentMethod)
			.cardNum(entity.cardNum)
			.cardName(entity.cardName)
			.pgProvider(entity.pgProvider);

		// items 매핑
		if (entity.items != null && !entity.items.isEmpty()) {
			List<com.limito.payment.domain.dto.PaymentItemDto> itemDtos =
				entity.items.stream()
					.filter(java.util.Objects::nonNull)
					.map(itemMapper::toDto)
					.collect(Collectors.toList());
			builder.items(itemDtos);
		}

		return builder.build();
	}
}