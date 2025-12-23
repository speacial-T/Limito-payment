package com.limito.payment.presentation;

import com.limito.payment.application.PaymentServiceV1;
import com.limito.payment.domain.dto.PaymentLogDetailDtoV1;
import com.limito.payment.presentation.dto.request.PortOneConfirmPaymentRequest;
import com.limito.payment.presentation.dto.request.RefundPaymentRequestV1;
import com.limito.payment.presentation.dto.response.ConfirmPaymentResponseV1;
import com.limito.payment.presentation.dto.response.FailLogPaymentResponseV1;
import com.limito.payment.presentation.dto.response.PaymentConfirmResponseDtoV1;
import com.limito.payment.presentation.dto.response.PaymentRefundResponseDtoV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentControllerV1 {

    private final PaymentServiceV1 paymentService;
    @Value("${portone.store-id}")
    private String storeId;
    @Value("${portone.channel-key}")
    private String channelKey;

    @GetMapping("/pass/{orderId}/page")
    public String showPaymentPage(
            @PathVariable("orderId") UUID orderId,
            Model model
    ) {

        PortOneConfirmPaymentRequest paymentData = paymentService.getPaymentDetailByOrderIdForPgRequest(orderId);
        model.addAttribute("orderId", orderId.toString());
        model.addAttribute("itemSummary", paymentData.getItemSummary());
        model.addAttribute("items", paymentData.getItems());
        model.addAttribute("totalPrice", paymentData.getTotalPrice());
        model.addAttribute("storeId", storeId);
        model.addAttribute("channelKey", channelKey);

        return "payment/portOne"; // templates/payment/portOne.html
    }

    @PostMapping("/pass/{paymentId}/confirm")
    public ResponseEntity<PaymentConfirmResponseDtoV1> confirmPayment(
            @PathVariable("paymentId") String paymentId,
            @RequestBody ConfirmPaymentResponseV1 response
    ) {
        PaymentConfirmResponseDtoV1 result =
                paymentService.confirmPayment(paymentId, response);
        log.info("PaymentControllerV1.confirmPayment called paymentKey={}, response= {}", paymentId, response);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/pass/{orderId}/fail-log")
    public ResponseEntity<Void> logPayment(
            @PathVariable("orderId") UUID orderId,
            @RequestBody FailLogPaymentResponseV1 response
    ) {
        log.info("PaymentControllerV1.logPayment response= {}", response);
        paymentService.recordConfirmFailLog(orderId, response);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<Void> refundPayment(
            @PathVariable UUID orderId,
            @RequestBody RefundPaymentRequestV1 request) {
        
        PaymentRefundResponseDtoV1 responseDtoV1 = paymentService.refundPayment(orderId, request);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<List<PaymentLogDetailDtoV1>> getPaymentByOrderId(
            @PathVariable("orderId") UUID orderId
    ) {
        List<PaymentLogDetailDtoV1> paymentLogDto = paymentService.getPaymentLogByOrderIdForAdmin(orderId);
        return ResponseEntity.ok(paymentLogDto);
    }
}
