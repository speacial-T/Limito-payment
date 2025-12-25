package com.limito.payment.application;

import com.limito.common.exception.AppException;
import com.limito.common.security.context.UserContext;
import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.domain.dto.PaymentItemDetailDtoV1;
import com.limito.payment.domain.dto.PaymentLogDetailDtoV1;
import com.limito.payment.domain.enums.PaymentStatusEnum;
import com.limito.payment.domain.enums.ProductTypeEnum;
import com.limito.payment.domain.enums.RefundStatusEnum;
import com.limito.payment.domain.model.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.limito.payment.domain.exception.PaymentErrorCode.*;

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
        PaymentEntity payment = paymentRepository.findByOrderId(orderId);
        payment.validateCanCreate();
        PaymentDetailDtoV1 paymentDto = paymentMapper.toDto(payment);

        List<PaymentItemDetailDtoV1> paymentItems = paymentItemRepository.getPaymentItems(paymentDto.getPaymentId())
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
        request.setItemSummary(paymentDto.getItemSummary());
        request.setItems(items);
        request.setTotalPrice(paymentDto.getTotalPrice());

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
        payment.validateCanConfirm();
        String rawJson = portOneWebClient.getPaymentRawPaymentInfoJson(paymentKey);
        PaymentDetailDtoV1 extra = portOnePaymentMapper.extractExtraInfo(rawJson);

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
        log.info("payment = {} ", payment);
        paymentRepository.save(payment);
        // 결제 결과 로그 기록
        // todo:로그 기록 부분 메소드로 리팩토링
        Optional<String> existingKey =
                paymentLogRepository.findIdempotencyKey(payment.internalId(),
                        PaymentStatusEnum.IN_PROGRESS,
                        RefundStatusEnum.NOT_REQUESTED);
        String idempotencyKey = existingKey
                .orElse(UUID.randomUUID().toString());
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
        PaymentDetailDtoV1 paymentDetail = paymentMapper.toDto(payment);
        return paymentMapper.forConfirmResponse(paymentDetail);
    }

    @Transactional(readOnly = true)
    public PaymentDetailDtoV1 getPaymentInfoByOrderId(UUID orderId) {
        return paymentMapper.toDto(paymentRepository.findByOrderId(orderId));
    }

    @Transactional
    public PaymentRefundResponseDtoV1 refundPayment(UUID orderId, RefundPaymentRequestV1 request) throws AppException {

        PaymentEntity payment = paymentRepository.findByOrderId(orderId);
        payment.validateCanRefund();
        // 환불 시도 로그 기록.
        // todo 로그 기록 보완
        PaymentLogEntity logEntity = paymentLogMapper.createRefundLog(payment, request.getRefundReason());
        logEntity = paymentLogRepository.save(logEntity);

        PaymentDetailDtoV1 detailDtoV1 = paymentMapper.toDto(payment);

        String rawJson = portOneWebClient.refundPayment(detailDtoV1.getPaymentKey(), request.getRefundReason());
        PaymentDetailDtoV1 result = portOnePaymentMapper.extractCancelInfo(rawJson);

        log.info("Payment refund successful for orderId={}, refundAt={}, reason={}", orderId,
                result.getRefundAt(), request.getRefundReason());

        payment.refund(request.getRefundReason(), result.getRefundAt(), result.getRefundStatus());
        paymentRepository.save(payment);
        // 환불 결과 로그 기록.
        PaymentLogEntity addRefundLogEntity = paymentLogMapper.addRefundLog(logEntity, result);
        log.info("addRefundLogEntity={}", addRefundLogEntity);
        addRefundLogEntity = paymentLogRepository.save(addRefundLogEntity);
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
        return null;
    }

    //결제 승인 실패 기록
    @Transactional
    public void recordConfirmFailLog(UUID orderId, FailLogPaymentResponseV1 response) {
        PaymentLogDetailDtoV1 logDetailDto = paymentLogMapper.mapFailLogToPaymentConfirmLog(response);
        log.info("logDetailDto={}", logDetailDto);
        PaymentEntity payment = paymentRepository.findByOrderId(orderId);
        Optional<String> existingKey =
                paymentLogRepository.findIdempotencyKey(payment.internalId(),
                        PaymentStatusEnum.IN_PROGRESS,
                        RefundStatusEnum.NOT_REQUESTED);
        String idempotencyKey = existingKey
                .orElse(UUID.randomUUID().toString());
        logDetailDto.setIdempotencyKey(idempotencyKey);
        int tryCount = paymentLogRepository.findMaxTryCount(payment.internalId(),
                PaymentStatusEnum.IN_PROGRESS,
                RefundStatusEnum.NOT_REQUESTED) + 1;
        logDetailDto.setTryCount(tryCount);
        log.info("logDetailDto={}", logDetailDto);
        PaymentLogEntity logEntity = paymentLogMapper.addConfirmLog(payment, logDetailDto);
        paymentLogRepository.save(logEntity);
    }

    public void userValidation(UUID orderId, UserContext user) {
        PaymentDetailDtoV1 detailDtoV1 = getPaymentInfoByOrderId(orderId);
        if (!detailDtoV1.getUserId().equals(user.getUserId())) {
            throw AppException.of(PAYMENT_REFUND_FAILED);
        }
    }


    public List<PaymentLogDetailDtoV1> getPaymentLogByOrderIdForAdmin(UUID orderId) {
        PaymentDetailDtoV1 payment = getPaymentInfoByOrderId(orderId);
        List<PaymentLogEntity> logs = paymentLogRepository.findAllByPaymentPaymentId(payment.getPaymentId());
        return logs.stream()
                .map(paymentLogMapper::toDto)
                .toList();
    }
}
