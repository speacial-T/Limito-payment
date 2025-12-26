package com.limito.payment.presentation;

import com.limito.common.security.auth.CurrentUser;
import com.limito.common.security.context.UserContext;
import com.limito.payment.application.PaymentServiceV1;
import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.infrastructure.dto.request.CreatePaymentRequestV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/internal/v1/payments")
@RequiredArgsConstructor
public class PaymentInternalControllerV1 {

    private final PaymentServiceV1 paymentService;

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Object> confirmPayment(
            @PathVariable UUID orderId,
            @RequestBody CreatePaymentRequestV1 request,
            @CurrentUser UserContext user) {
        try {
            paymentService.validPaymentRequest(orderId, request);
        } catch (Exception e) {
            log.warn("주문 아이디 orderId={}에 대한 결제 요청 내역이 있습니다. {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(request);
        }
        paymentService.createPayment(orderId, request, user.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentDetailDtoV1> getPaymentByOrderId(
            @PathVariable("orderId") UUID orderId,
            @CurrentUser UserContext user
    ) {
        paymentService.userValidation(orderId, user);
        PaymentDetailDtoV1 paymentDto = paymentService.getPaymentInfoByOrderId(orderId);
        return ResponseEntity.ok(paymentDto);
    }
}
