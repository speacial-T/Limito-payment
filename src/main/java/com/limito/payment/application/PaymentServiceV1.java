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
import com.limito.payment.domain.dto.PaymentLogDetailDtoV1;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.ProductTypeEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;
import com.limito.payment.domain.model.PaymentEntity;
import com.limito.payment.domain.model.PaymentItemEntity;
import com.limito.payment.domain.model.PaymentItemMapper;
import com.limito.payment.domain.model.PaymentLogEntity;
import com.limito.payment.domain.model.PaymentLogMapper;
import com.limito.payment.domain.model.PaymentMapper;
import com.limito.payment.domain.repository.PaymentItemRepository;
import com.limito.payment.domain.repository.PaymentLogRepository;
import com.limito.payment.domain.repository.PaymentRepository;
import com.limito.payment.infrastructure.client.OrderClient;
import com.limito.payment.infrastructure.client.portone.PortOneClient;
import com.limito.payment.infrastructure.client.portone.mapper.PortOnePaymentMapper;
import com.limito.payment.infrastructure.dto.request.CreatePaymentRequestV1;
import com.limito.payment.infrastructure.dto.request.OrderItem;
import com.limito.payment.presentation.dto.request.PortOneConfirmPaymentRequest;
import com.limito.payment.presentation.dto.request.RefundPaymentRequestV1;
import com.limito.payment.presentation.dto.response.ConfirmPaymentResponseV1;
import com.limito.payment.presentation.dto.response.FailLogPaymentResponseV1;
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
	private final PaymentLogRepository paymentLogRepository;

	private final PortOneClient portOneWebClient;

	private final PortOnePaymentMapper portOnePaymentMapper;
	private final PaymentMapper paymentMapper;
	private final PaymentItemMapper paymentItemMapper;
	private final PaymentLogMapper paymentLogMapper;

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
		PaymentLogDetailDtoV1 logDetailDto = paymentLogMapper.createConfirmLog(payment);
		log.info("logDetailDto={}", logDetailDto);
		PaymentLogEntity logEntity = paymentLogMapper.addConfirmLog(payment, logDetailDto);
		logEntity = paymentLogRepository.save(logEntity);
	}

	@Transactional
	public PaymentConfirmResponseDtoV1 confirmPayment(
		String paymentKey,
		ConfirmPaymentResponseV1 response
	) {
		log.info("[confirmPayment] paymentKey={}, orderId={}", paymentKey, response.getOrderId());
		UUID orderId = UUID.fromString(response.getOrderId());
		PaymentEntity payment = paymentRepository.findByOrderId(orderId);
		String rawJson = portOneWebClient.getPaymentRawPaymentInfoJson(paymentKey);
		PaymentDetailDtoV1 extra = portOnePaymentMapper.extractExtraInfo(rawJson);
		// 결제 결과 로그 기록
		// todo:로그 기록 부분 메소드로 리팩토링
		String idempotencyKey = String.valueOf(paymentLogRepository.findIdempotencyKey(payment.internalId(),
			PaymentStatusEnum.IN_PROGRESS,
			RefundStatusEnum.NOT_REQUESTED));
		int tryCount = paymentLogRepository.findMaxTryCount(payment.internalId(),
			PaymentStatusEnum.IN_PROGRESS,
			RefundStatusEnum.NOT_REQUESTED) + 1;
		PaymentLogDetailDtoV1 confirmLogDetailDto = paymentLogMapper.mapToPaymentConfirmLog(response);
		confirmLogDetailDto.setIdempotencyKey(idempotencyKey);
		confirmLogDetailDto.setTryCount(tryCount);
		confirmLogDetailDto.setRequestPayload(response.getRequestPayload());
		confirmLogDetailDto.setResponsePayload(rawJson);
		log.info("logDetailDto={}", confirmLogDetailDto);
		PaymentLogEntity logEntity = paymentLogMapper.addConfirmLog(payment, confirmLogDetailDto);
		logEntity = paymentLogRepository.save(logEntity);

		ProductTypeEnum type = paymentItemRepository.getProductTypeByPaymentId(payment.internalId());
		boolean isLimited = (type == ProductTypeEnum.LIMITED);
		//주문 서비스로 결과 전달
		// TODO: refactor - if/else
		if (extra.getPaymentStatus() == PaymentStatusEnum.SUCCESS) { //결제 성공
			if (isLimited) { //한정상품
				orderClient.notifyPaymentLimitedSuccess(orderId);
			} else { //리셀상품
				orderClient.notifyPaymentResellSuccess(orderId);
			}
		} else { //결제 실패
			if (isLimited) {
				orderClient.notifyLimitedPaymentFail(orderId);
			} else {
				orderClient.notifyResellPaymentFail(orderId);
			}
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

	//결제 승인 실패 기록
	@Transactional
	public void recordConfirmFailLog(UUID orderId, FailLogPaymentResponseV1 response) {
		PaymentLogDetailDtoV1 logDetailDto = paymentLogMapper.mapFailLogToPaymentConfirmLog(response);
		log.info("logDetailDto={}", logDetailDto);
		PaymentEntity payment = paymentRepository.findByOrderId(orderId);
		String idempotencyKey = String.valueOf(paymentLogRepository.findIdempotencyKey(payment.internalId(),
			PaymentStatusEnum.IN_PROGRESS,
			RefundStatusEnum.NOT_REQUESTED));
		logDetailDto.setIdempotencyKey(idempotencyKey);
		int tryCount = paymentLogRepository.findMaxTryCount(payment.internalId(),
			PaymentStatusEnum.IN_PROGRESS,
			RefundStatusEnum.NOT_REQUESTED) + 1;
		logDetailDto.setTryCount(tryCount);
		log.info("logDetailDto={}", logDetailDto);
		PaymentLogEntity logEntity = paymentLogMapper.addConfirmLog(payment, logDetailDto);
		paymentLogRepository.save(logEntity);
	}
}
