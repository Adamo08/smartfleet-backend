package com.adamo.vrspfab.payments;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @PostMapping("/confirm/{sessionId}")
    public ResponseEntity<PaymentResponseDto> confirmPayment(@PathVariable String sessionId) {
        return ResponseEntity.ok(paymentService.confirmPayment(sessionId));
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponseDto> getPaymentStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(paymentId));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentDto> getPaymentByReservationId(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getPaymentByReservationId(reservationId));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<PaymentDto>> getUserPaymentHistory(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getUserPaymentHistory(pageable));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long paymentId) {
        paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refund")
    public ResponseEntity<RefundResponseDto> processRefund(@Valid @RequestBody RefundRequestDto requestDto) {
        return ResponseEntity.ok(refundService.processRefund(requestDto));
    }

    @GetMapping("/refund/{refundId}")
    public ResponseEntity<RefundDetailsDto> getRefundDetails(@PathVariable Long refundId) {
        return ResponseEntity.ok(refundService.getRefundDetails(refundId));
    }

    @GetMapping("/refunds")
    public ResponseEntity<Page<RefundDetailsDto>> getRefundHistory(Pageable pageable) {
        return ResponseEntity.ok(refundService.getRefundHistory(pageable));
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

    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsDto> getPaymentStats() {
        return ResponseEntity.ok(paymentService.getPaymentStats());
    }

    @GetMapping("/methods")
    public ResponseEntity<PaymentMethodsDto> getPaymentMethods() {
        return ResponseEntity.ok(paymentService.getPaymentMethods());
    }

    @GetMapping("/methods/{methodId}/validate")
    public ResponseEntity<PaymentMethodValidationDto> validatePaymentMethod(@PathVariable String methodId) {
        return ResponseEntity.ok(paymentService.validatePaymentMethod(methodId));
    }
}
