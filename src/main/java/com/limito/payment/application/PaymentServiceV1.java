package com.limito.payment.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.limito.payment.domain.dto.PaymentDto;
import com.limito.payment.domain.dto.PaymentItemDto;
import com.limito.payment.domain.model.PaymentEntity;
import com.limito.payment.domain.model.PaymentItemEntity;
import com.limito.payment.domain.model.PaymentItemMapper;
import com.limito.payment.domain.model.PaymentMapper;
import com.limito.payment.domain.repository.PaymentItemRepository;
import com.limito.payment.domain.repository.PaymentRepository;
import com.limito.payment.infrastructure.client.portone.PortOneClient;
import com.limito.payment.infrastructure.client.portone.mapper.PortOnePaymentMapper;
import com.limito.payment.presentation.dto.request.CreatePaymentRequestV1;
import com.limito.payment.presentation.dto.request.OrderItem;
import com.limito.payment.presentation.dto.request.PortOneConfirmPaymentRequest;
import com.limito.payment.presentation.dto.response.ConfirmPaymentResponseV1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceV1 {

	private final PaymentRepository paymentRepository;
	private final PaymentItemRepository paymentItemRepository;
	private final PortOneClient portOneWebClient;
	private final PortOnePaymentMapper portOnePaymentMapper;
	private final PaymentMapper paymentMapper;
	private final PaymentItemMapper paymentItemMapper;

	@Transactional
	public PortOneConfirmPaymentRequest getPaymentDetailByOrderIdForPgRequest(
		UUID orderId
	) {
		PortOneConfirmPaymentRequest request = new PortOneConfirmPaymentRequest();
		PaymentDto payment = paymentMapper.toDto(paymentRepository.getByOrderId(orderId));

		List<PaymentItemDto> paymentItems = paymentItemRepository.getPaymentItems(payment.getPaymentId())
			.stream()
			.map(paymentItemMapper::toDto)
			.toList();
		List<OrderItem> items = paymentItems.stream()
			.map(p -> OrderItem.builder()
				.productName(p.getProductName())
				.quantity(p.getProductAmount())
				.productPrice(p.getProductPrice())
				.sellerId(p.getSellerId())
				.build())
			.toList();
		request.setOrderId(orderId);
		request.setItemSummary(payment.getItemSummary());
		request.setItems(items);
		request.setTotalPrice(payment.getTotalPrice());
		return request;
	}

	public void validatePaymentRequestUniqueness(UUID orderId) {
		if (paymentRepository.hasPaymentByOrderId(orderId)) {
			throw new IllegalArgumentException("Payment already exists for orderId: " + orderId);
		}
	}

	@Transactional
	public void createPayment(UUID orderId, CreatePaymentRequestV1 request) {
		log.info("received createPayment for orderId={}, request={}", orderId, request);
		List<PaymentItemDto> paymentItems = request.getItems().stream()
			.map(PaymentItemDto::mapToPaymentItem)
			.toList();
		log.debug("mapped paymentItems={}", paymentItems);
		PaymentEntity payment = PaymentEntity.create(orderId, request.getItemSummary(), request.getTotalPrice());

		PaymentEntity savedPayment = paymentRepository.save(payment);

		List<PaymentItemDto> itemDtos = request.getItems().stream()
			.map(PaymentItemDto::mapToPaymentItem)
			.toList();

		List<PaymentItemEntity> itemEntities = itemDtos.stream()
			.map(paymentItemMapper::toEntity)
			.toList();
		payment.addItems(itemEntities);

		paymentItemRepository.saveAll(itemEntities);
	}

	@Transactional
	public PaymentDto confirmPayment(
		String paymentKey,
		ConfirmPaymentResponseV1 response
	) {
		log.info("[confirmPayment] paymentKey={}, orderId={}", paymentKey, response.getOrderId());
		UUID orderId = UUID.fromString(response.getOrderId());
		PaymentEntity payment = paymentRepository.getByOrderId(orderId);
		if (payment == null) {
			throw new IllegalStateException("PaymentEntity not found for orderId=" + orderId);
		}

		String rawJson = portOneWebClient.getPaymentRawPaymentInfoJson(paymentKey);
		PaymentDto extra = portOnePaymentMapper.extractExtraInfo(rawJson);

		// 결제 완료/실패등 상태 반영
		payment.handlePgCallback(extra);
		paymentRepository.save(payment);
		List<PaymentItemEntity> items =
			paymentItemRepository.getPaymentItems(payment.internalId());

		log.debug("[confirmPayment] loaded paymentItems={}", items);

		items.forEach(item -> {
			item.updateStatus(extra.getPaymentStatus());
			item.assignPayment(payment);
		});

		paymentItemRepository.saveAll(items);
		PaymentDto result = paymentMapper.toDto(payment);
		return result;
	}
}
