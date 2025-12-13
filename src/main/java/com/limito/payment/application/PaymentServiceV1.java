package com.limito.payment.application;

import static com.limito.payment.domain.exception.PaymentErrorCode.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.limito.common.exception.AppException;
import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.domain.dto.PaymentItemDetailDtoV1;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.ProductTypeEnum;
import com.limito.payment.domain.model.PaymentEntity;
import com.limito.payment.domain.model.PaymentItemEntity;
import com.limito.payment.domain.model.PaymentItemMapper;
import com.limito.payment.domain.model.PaymentMapper;
import com.limito.payment.domain.repository.PaymentItemRepository;
import com.limito.payment.domain.repository.PaymentRepository;
import com.limito.payment.infrastructure.client.OrderClient;
import com.limito.payment.infrastructure.client.portone.PortOneClient;
import com.limito.payment.infrastructure.client.portone.mapper.PortOnePaymentMapper;
import com.limito.payment.infrastructure.dto.request.CreatePaymentRequestV1;
import com.limito.payment.infrastructure.dto.request.OrderItem;
import com.limito.payment.presentation.dto.request.PortOneConfirmPaymentRequest;
import com.limito.payment.presentation.dto.request.RefundPaymentRequestV1;
import com.limito.payment.presentation.dto.response.ConfirmPaymentResponseV1;
import com.limito.payment.presentation.dto.response.PaymentConfirmResponseDtoV1;
import com.limito.payment.presentation.dto.response.PaymentRefundResponseDtoV1;

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
	private final OrderClient orderClient;

	@Transactional
	public PortOneConfirmPaymentRequest getPaymentDetailByOrderIdForPgRequest(
		UUID orderId
	) {
		PortOneConfirmPaymentRequest request = new PortOneConfirmPaymentRequest();
		PaymentDetailDtoV1 payment = paymentMapper.toDto(paymentRepository.findByOrderId(orderId));

		List<PaymentItemDetailDtoV1> paymentItems = paymentItemRepository.getPaymentItems(payment.getPaymentId())
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

	public void validPaymentRequest(UUID orderId, CreatePaymentRequestV1 request) {
		if (paymentRepository.hasPaymentByOrderId(orderId)) {
			log.warn("Duplicate payment request for orderId={}", orderId);
			throw AppException.of(PAYMENT_DUPLICATE_ORDER);

		}
		int totalCalculatedPrice = request.getItems().stream()
			.mapToInt(item -> item.getProductPrice() * item.getQuantity())
			.sum();
		if (request.getTotalPrice() != totalCalculatedPrice) {
			log.warn("Payment total price mismatch for orderId={}: expected={}, actual={}",
				orderId, totalCalculatedPrice, request.getTotalPrice());
			throw AppException.of(PAYMENT_TOTAL_PRICE_ERROR);
		}
	}

	@Transactional
	public void createPayment(UUID orderId, CreatePaymentRequestV1 request) {
		log.info("received createPayment for orderId={}, request={}", orderId, request);
		List<PaymentItemDetailDtoV1> paymentItems = request.getItems()
			.stream()
			.map(paymentItemMapper::mapToPaymentItem)
			.toList();
		log.debug("mapped paymentItems={}", paymentItems);
		PaymentEntity payment = PaymentMapper.create(orderId, request);
		PaymentEntity savedPayment = paymentRepository.save(payment);

		List<PaymentItemEntity> itemEntities = paymentItems.stream()
			.map(paymentItemMapper::toEntity)
			.toList();
		savedPayment.addItems(itemEntities);
		paymentItemRepository.saveAll(itemEntities);
	}

	@Transactional
	public PaymentConfirmResponseDtoV1 confirmPayment(
		String paymentKey,
		ConfirmPaymentResponseV1 response
	) {

		log.info("[confirmPayment] paymentKey={}, orderId={}", paymentKey, response.getOrderId());
		UUID orderId = UUID.fromString(response.getOrderId());
		PaymentEntity payment = paymentRepository.findByOrderId(orderId);
		if (payment == null) {
			throw new AppException(PAYMENT_NOT_FOUND);
		}

		String rawJson = portOneWebClient.getPaymentRawPaymentInfoJson(paymentKey);
		PaymentDetailDtoV1 extra = portOnePaymentMapper.extractExtraInfo(rawJson);
		//주문 서비스로 결과 전달
		// TODO: refactor - if/else
		if (extra.getPaymentStatus() == PaymentStatusEnum.SUCCESS) {
			ProductTypeEnum type = paymentItemRepository.getProductTypeByPaymentId(payment.internalId());
			if (type == ProductTypeEnum.LIMITED) {
				orderClient.notifyPaymentLimitedSuccess(orderId);
			} else {
				orderClient.notifyPaymentResellSuccess(orderId);
			}
		} else {
			orderClient.notifyPaymentFail(orderId);
		}
		// 결제 완료/실패 등 상태 반영
		payment.handlePgCallback(extra);
		paymentRepository.save(payment);

		PaymentDetailDtoV1 paymentDetail = paymentMapper.toDto(payment);
		return paymentMapper.forConfirmResponse(paymentDetail);
	}

	@Transactional(readOnly = true)
	public PaymentDetailDtoV1 getPaymentInfoByOrderId(UUID orderId) {
		return paymentMapper.toDto(paymentRepository.findByOrderId(orderId));
	}

	@Transactional
	public PaymentRefundResponseDtoV1 refundPayment(UUID orderId, RefundPaymentRequestV1 request) {

		PaymentEntity payment = paymentRepository.findByOrderId(orderId);

		payment.validateCanRefund();

		PaymentDetailDtoV1 detailDtoV1 = paymentMapper.toDto(payment);

		String rawJson = portOneWebClient.cancelPayment(detailDtoV1.getPaymentKey(), request.getRefundReason());
		PaymentDetailDtoV1 result = portOnePaymentMapper.extractCancelInfo(rawJson);

		log.info("Payment refund successful for orderId={}, refundAt={}, reason={}", orderId,
			result.getRefundAt(), request.getRefundReason());
		payment.refund(request.getRefundReason(), result.getRefundAt(), result.getRefundStatus());
		PaymentRefundResponseDtoV1 paymentRefundResponseDto = paymentMapper.forRefundResponse(detailDtoV1);
		if (result.getPaymentStatus() == PaymentStatusEnum.REFUND) {
			ProductTypeEnum type = paymentItemRepository.getProductTypeByPaymentId(detailDtoV1.getPaymentId());
			try {
				if (type == ProductTypeEnum.LIMITED) {
					orderClient.notifyPaymentRefundLimitedSuccess(orderId, paymentRefundResponseDto);
				} else {
					orderClient.notifyPaymentRefundResellSuccess(orderId, paymentRefundResponseDto);
				}
			} catch (AppException e) {
				throw AppException.of(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
			}
		}
		return paymentRefundResponseDto;
	}

}
