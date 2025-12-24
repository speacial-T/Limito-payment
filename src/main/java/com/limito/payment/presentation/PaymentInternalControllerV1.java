package com.limito.payment.presentation;

import com.limito.payment.application.PaymentServiceV1;
import com.limito.payment.domain.dto.PaymentDetailDtoV1;
import com.limito.payment.infrastructure.dto.request.CreatePaymentRequestV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            @RequestBody CreatePaymentRequestV1 request) {
        paymentService.validPaymentRequest(orderId, request);
        paymentService.createPayment(orderId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentDetailDtoV1> getPaymentByOrderId(
            @PathVariable("orderId") UUID orderId
    ) {
        PaymentDetailDtoV1 paymentDto = paymentService.getPaymentInfoByOrderId(orderId);
        return ResponseEntity.ok(paymentDto);
    }
}
