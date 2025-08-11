package com.adamo.vrspfab.payments;

import jakarta.validation.Valid;
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
    public ResponseEntity<SessionResponseDto> createPaymentSession(@Valid @RequestBody SessionRequestDto requestDto) {
        return ResponseEntity.ok(paymentService.createPaymentSession(requestDto));
    }
    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @Valid @RequestBody PaymentRequestDto requestDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // The idempotency key is passed to the service layer for handling
        return ResponseEntity.ok(paymentService.processPayment(requestDto, idempotencyKey));
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponseDto> getPaymentStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(paymentId));
    }

    @PostMapping("/refund")
    public ResponseEntity<RefundResponseDto> processRefund(@Valid @RequestBody RefundRequestDto requestDto) {
        return ResponseEntity.ok(refundService.processRefund(requestDto));
    }

    @GetMapping("/refund/{refundId}")
    public ResponseEntity<RefundDetailsDto> getRefundDetails(@PathVariable Long refundId) {
        return ResponseEntity.ok(refundService.getRefundDetails(refundId));
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsReportDto> getAnalytics(
            @RequestParam (required = false) String startDate,
            @RequestParam (required = false) String endDate) {

        // Default to the last 30 days if no dates are provided
        if (startDate == null || endDate == null) {
            LocalDateTime now = LocalDateTime.now();
            startDate = now.minusDays(30).format(DateTimeFormatter.ISO_DATE_TIME);
            endDate = now.format(DateTimeFormatter.ISO_DATE_TIME);
        }

        LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
        return ResponseEntity.ok(analyticsService.getPaymentAnalytics(start, end));
    }
}
