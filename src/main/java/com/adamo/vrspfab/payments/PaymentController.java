package com.adamo.vrspfab.payments;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for processing payments and managing payment-related operations")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final RefundService refundService;
    private final PaymentAnalyticsService analyticsService;

    @Operation(summary = "Create a payment session",
               description = "Initiates a payment session for a reservation, typically for external payment gateways.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment session created successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid session request"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/session")
    public ResponseEntity<SessionResponseDto> createPaymentSession(@Valid @RequestBody SessionRequestDto requestDto) {
        return ResponseEntity.ok(paymentService.createPaymentSession(requestDto));
    }
    
    @Operation(summary = "Process a payment",
               description = "Processes a payment with an optional idempotency key to prevent duplicate transactions.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid payment request"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @Valid @RequestBody PaymentRequestDto requestDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // The idempotency key is passed to the service layer for handling
        return ResponseEntity.ok(paymentService.processPayment(requestDto, idempotencyKey));
    }

    @Operation(summary = "Confirm a payment",
               description = "Confirms a payment using a session ID, typically after an external payment gateway redirect.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment confirmed successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid session ID or payment already confirmed"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "404", description = "Session not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/confirm/{sessionId}")
    public ResponseEntity<PaymentResponseDto> confirmPayment(@PathVariable String sessionId) {
        return ResponseEntity.ok(paymentService.confirmPayment(sessionId));
    }

    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam(required = false) String token, @RequestParam(required = false) String PayerID) {
        logger.info("=== PAYPAL SUCCESS REDIRECT RECEIVED ===");
        logger.info("Token: {}", token);
        logger.info("PayerID: {}", PayerID);
        logger.info("Request URL: /payments/success");
        
        // PayPal redirects here after successful payment
        // First, capture the payment using the token
        if (token != null && !token.isEmpty()) {
            logger.info("Attempting to confirm PayPal payment with token: {}", token);
            try {
                // Confirm the PayPal payment using the existing confirmPayment method
                var response = paymentService.confirmPayment(token);
                logger.info("Payment confirmation successful: {}", response);
            } catch (Exception e) {
                // Log error but continue with redirect
                logger.error("Error confirming PayPal payment: {}", e.getMessage(), e);
                System.err.println("Error confirming PayPal payment: " + e.getMessage());
            }
        } else {
            logger.warn("No token received from PayPal - cannot confirm payment");
        }
        
        // Return HTML that automatically redirects to frontend
        String redirectUrl = "/payments/success?status=success";
        if (token != null && !token.isEmpty()) {
            redirectUrl += "&token=" + token;
        }
        if (PayerID != null && !PayerID.isEmpty()) {
            redirectUrl += "&PayerID=" + PayerID;
        }
        
        logger.info("Redirecting to frontend URL: {}", redirectUrl);
        
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Payment Successful</title>
                <meta http-equiv="refresh" content="0;url=%s">
            </head>
            <body>
                <p>Payment successful! Redirecting to SmartFleet...</p>
                <script>window.location.href = '%s';</script>
            </body>
            </html>
            """.formatted(redirectUrl, redirectUrl);
        
        logger.info("Returning HTML redirect page");
        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(html);
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> paymentCancel() {
        // PayPal redirects here when payment is cancelled
        // Return HTML that automatically redirects to frontend
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Payment Cancelled</title>
                <meta http-equiv="refresh" content="0;url=/payments/cancel?status=cancelled">
            </head>
            <body>
                <p>Payment cancelled! Redirecting to SmartFleet...</p>
                <script>window.location.href = '/payments/cancel?status=cancelled';</script>
            </body>
            </html>
            """;
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(html);
    }

    @Operation(summary = "Get payment status",
               description = "Retrieves the status of a payment by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment status retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "404", description = "Payment not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponseDto> getPaymentStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(paymentId));
    }

    @Operation(summary = "Get payment by ID",
               description = "Retrieves detailed information about a payment by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "404", description = "Payment not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @Operation(summary = "Get payment by reservation ID",
               description = "Retrieves payment information associated with a specific reservation ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "404", description = "Payment for reservation not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentDto> getPaymentByReservationId(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getPaymentByReservationId(reservationId));
    }

    @Operation(summary = "Get user payment history",
               description = "Retrieves a paginated list of payment transactions for the authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment history retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/history")
    public ResponseEntity<Page<PaymentDto>> getUserPaymentHistory(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getUserPaymentHistory(pageable));
    }

    @Operation(summary = "Get filtered user payment history",
               description = "Retrieves a filtered and paginated list of payment transactions for the authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Filtered payment history retrieved successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/history/filtered")
    public ResponseEntity<Page<PaymentDto>> getUserPaymentHistoryFiltered(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String searchTerm
    ) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDirection), sortBy));
        
        PaymentFilter filter = PaymentFilter.builder()
                .status(status)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .startDate(startDate)
                .endDate(endDate)
                .searchTerm(searchTerm)
                .build();
        
        return ResponseEntity.ok(paymentService.getUserPaymentHistoryWithFilter(filter, pageable));
    }

    @Operation(summary = "Cancel a payment",
               description = "Cancels a payment by its ID. This might initiate a refund if the payment was already captured.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment cancelled successfully"),
                       @ApiResponse(responseCode = "400", description = "Cannot cancel payment (e.g., already refunded)"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "404", description = "Payment not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long paymentId) {
        paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Process a refund",
               description = "Initiates a refund for a given payment.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid refund request or payment not eligible for refund"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "404", description = "Payment not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/refund")
    public ResponseEntity<RefundResponseDto> processRefund(@Valid @RequestBody RefundRequestDto requestDto) {
        return ResponseEntity.ok(refundService.processRefund(requestDto));
    }

    @Operation(summary = "Get refund details",
               description = "Retrieves detailed information about a specific refund by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Refund details retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "404", description = "Refund not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/refund/{refundId}")
    public ResponseEntity<RefundDetailsDto> getRefundDetails(@PathVariable Long refundId) {
        return ResponseEntity.ok(refundService.getRefundDetails(refundId));
    }

    @Operation(summary = "Get refund history",
               description = "Retrieves a paginated list of all refunds.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Refund history retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/refunds")
    public ResponseEntity<Page<RefundDetailsDto>> getRefundHistory(Pageable pageable) {
        return ResponseEntity.ok(refundService.getRefundHistory(pageable));
    }

    @Operation(summary = "Get payment analytics",
               description = "Retrieves payment analytics and statistics for a given date range. If no dates are provided, defaults to the last 30 days.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Analytics report retrieved successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid date format"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
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

    @Operation(summary = "Get payment statistics",
               description = "Retrieves general payment statistics, such as total revenue, successful transactions, etc.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment statistics retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsDto> getPaymentStats() {
        return ResponseEntity.ok(paymentService.getPaymentStats());
    }

    @Operation(summary = "Get available payment methods",
               description = "Retrieves a list of available payment methods configured in the system.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment methods retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/methods")
    public ResponseEntity<PaymentMethodsDto> getPaymentMethods() {
        return ResponseEntity.ok(paymentService.getPaymentMethods());
    }

    @Operation(summary = "Validate a payment method",
               description = "Validates the details of a specific payment method.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment method validated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid payment method details"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "404", description = "Payment method not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/methods/{methodId}/validate")
    public ResponseEntity<PaymentMethodValidationDto> validatePaymentMethod(@PathVariable String methodId) {
        return ResponseEntity.ok(paymentService.validatePaymentMethod(methodId));
    }

    @Operation(summary = "Get all payments (Admin only)",
            description = "Retrieves a paginated and filtered list of all payments in the system. Requires admin privileges.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved payments"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @GetMapping("/admin")
    public ResponseEntity<Page<PaymentDto>> getAllPaymentsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long reservationId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String searchTerm
    ) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDirection), sortBy));
        PaymentFilter filter = PaymentFilter.builder()
                .userId(userId)
                .reservationId(reservationId)
                .status(status)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .startDate(startDate)
                .endDate(endDate)
                .searchTerm(searchTerm)
                .build();
        return ResponseEntity.ok(paymentService.getAllPaymentsAdmin(filter, pageable));
    }

    @Operation(summary = "Delete a payment (Admin only)",
            description = "Deletes a payment from the system. Requires admin privileges.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Payment deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges"),
                    @ApiResponse(responseCode = "404", description = "Payment not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @DeleteMapping("/admin/{paymentId}")
    public ResponseEntity<Void> deletePaymentAdmin(@PathVariable Long paymentId) {
        paymentService.deletePayment(paymentId);
        return ResponseEntity.noContent().build();
    }
}
