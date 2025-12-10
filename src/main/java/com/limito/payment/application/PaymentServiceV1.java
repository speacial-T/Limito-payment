package com.limito.payment.application;

import static com.limito.payment.domain.exception.PaymentErrorCode.*;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.limito.common.exception.AppException;
import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.domain.dto.PaymentItemDetailDtoV1;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.ProductTypeEnum;
import com.limito.payment.domain.enums.CancelAndRefundStatusEnum;
import com.limito.payment.domain.model.PaymentEntity;
import com.limito.payment.domain.model.PaymentItemEntity;
import com.limito.payment.domain.model.PaymentItemMapper;
import com.limito.payment.domain.model.PaymentMapper;
import com.limito.payment.domain.repository.PaymentItemRepository;
import com.limito.payment.domain.repository.PaymentRepository;
import com.limito.payment.infrastructure.client.portone.OrderClient;
import com.limito.payment.infrastructure.client.portone.PortOneClient;
import com.limito.payment.infrastructure.client.portone.mapper.PortOnePaymentMapper;
import com.limito.payment.infrastructure.dto.request.CreatePaymentRequestV1;
import com.limito.payment.infrastructure.dto.request.OrderItem;
import com.limito.payment.presentation.dto.request.CancelAndRefundPaymentRequestV1;
import com.limito.payment.presentation.dto.request.PortOneConfirmPaymentRequest;
import com.limito.payment.presentation.dto.response.ConfirmPaymentResponseV1;
import com.limito.payment.presentation.dto.response.PaymentConfirmResponseDtoV1;

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
		PaymentDetailDtoV1 payment = paymentMapper.toDto(paymentRepository.getByOrderId(orderId));

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
			throw new AppException(PAYMENT_DUPLICATE_ORDER);
		}
		int totalCalculatedPrice = request.getItems().stream()
			.mapToInt(item -> item.getProductPrice() * item.getQuantity())
			.sum();
		if (request.getTotalPrice() != totalCalculatedPrice) {
			log.warn("Payment total price mismatch for orderId={}: expected={}, actual={}",
				orderId, totalCalculatedPrice, request.getTotalPrice());
			throw new AppException(PAYMENT_TOTAL_PRICE_ERROR);
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
		PaymentEntity payment = paymentRepository.getByOrderId(orderId);
		if (payment == null) {
			throw new AppException(PAYMENT_NOT_FOUND);
		}

		String rawJson = portOneWebClient.getPaymentRawPaymentInfoJson(paymentKey);
		PaymentDetailDtoV1 extra = portOnePaymentMapper.extractExtraInfo(rawJson);
		List<PaymentItemEntity> items =
			paymentItemRepository.getPaymentItems(payment.internalId());
		List<PaymentItemDetailDtoV1> dtoList = items.stream()
			.map(paymentItemMapper::toDto)
			.toList();
		//주문 서비스로 결과 전달
		// TODO: refactor - if/else
		if (extra.getPaymentStatus() == PaymentStatusEnum.SUCCESS) {
			if(dtoList.get(0).getProductType()== ProductTypeEnum.LIMITED){
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

		log.debug("[confirmPayment] loaded paymentItems={}", items);

		items.forEach(item -> {
			item.updateStatus(extra.getPaymentStatus());
			item.assignPayment(payment);
		});

		paymentItemRepository.saveAll(items);
		PaymentDetailDtoV1 paymentDetail = paymentMapper.toDto(payment);
		PaymentConfirmResponseDtoV1 result = PaymentConfirmResponseDtoV1.builder()
			.orderId(paymentDetail.getOrderId())
			.paymentStatus(paymentDetail.getPaymentStatus())
			.paymentMethod(paymentDetail.getPaymentMethod())
			.approvedAt(paymentDetail.getApprovedAt())
			.build();
		return result;
	}

	@Transactional(readOnly = true)
	public PaymentDetailDtoV1 getPaymentInfoByOrderId(UUID orderId) {
		return paymentMapper.toDto(paymentRepository.getByOrderId(orderId));
	}

	@Transactional
	public void cancelAndRefundPayment(UUID orderId, CancelAndRefundPaymentRequestV1 request) {
		try {
			PaymentEntity payment = paymentRepository.getByOrderId(orderId);
			try {
				payment.validateCanCancelOrRefund();

			} catch (AppException e) {
				return;
			}
			PaymentDetailDtoV1 detailDtoV1 = paymentMapper.toDto(payment);

			String rawJson = portOneWebClient.cancelPayment(detailDtoV1.getPaymentKey(), request.getRefundReason());
			PaymentDetailDtoV1 result = portOnePaymentMapper.extractCancelInfo(rawJson);
			if (result.getFailLog() != null) {
				payment.markAsCancelFailed(result.getFailLog());
				return;
			}
			log.info("Payment cancellation/refund successful for orderId={}, refundAt={}, reason={}", orderId,
				result.getRefundAt(), request.getRefundReason());
			payment.cancelAndRefund(request.getRefundReason(), result.getRefundAt(), request.getCancelType());
			List<PaymentItemEntity> paymentItems = paymentItemRepository.getPaymentItems(detailDtoV1.getPaymentId());
			paymentItems.forEach(item -> {
				item.updateCancelAndRefundStatus(request.getCancelType());
				item.assignPayment(payment);
			});
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

}
