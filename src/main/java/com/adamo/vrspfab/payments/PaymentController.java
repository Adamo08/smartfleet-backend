package com.adamo.vrspfab.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;
    private final PaymentAnalyticsService analyticsService;

    @PostMapping("/session")
    public ResponseEntity<SessionResponseDto> createPaymentSession(@RequestBody SessionRequestDto requestDto) {
        return ResponseEntity.ok(paymentService.createPaymentSession(requestDto));
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @RequestBody PaymentRequestDto requestDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // The idempotency key is passed to the service layer for handling
        return ResponseEntity.ok(paymentService.processPayment(requestDto, idempotencyKey));
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponseDto> getPaymentStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(paymentId));
    }

    @PostMapping("/refund")
    public ResponseEntity<RefundResponseDto> processRefund(@RequestBody RefundRequestDto requestDto) {
        return ResponseEntity.ok(refundService.processRefund(requestDto));
    }

    @GetMapping("/refund/{refundId}")
    public ResponseEntity<Refund> getRefundDetails(@PathVariable Long refundId) {
        return ResponseEntity.ok(refundService.getRefundDetails(refundId));
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsReportDto> getAnalytics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
        return ResponseEntity.ok(analyticsService.getPaymentAnalytics(start, end));
    }
}
