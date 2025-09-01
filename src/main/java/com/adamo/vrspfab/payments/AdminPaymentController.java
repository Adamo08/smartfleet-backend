package com.adamo.vrspfab.payments;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for administrator-only payment management operations.
 * All endpoints in this controller require ADMIN role.
 */
@RestController
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Payment Management", description = "APIs for administrators to manage all payments and refunds")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;
    private final RefundService refundService;

    /**
     * Retrieves a paginated list of all payments.
     * @param pageable Pagination information.
     * @return A page of Payment entities.
     */
    @Operation(summary = "Get all payments (Admin only)",
               description = "Retrieves a paginated list of all payments in the system. Requires ADMIN role.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved payments"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public ResponseEntity<Page<PaymentDetailsDto>> getAllPayments(Pageable pageable) {
        return ResponseEntity.ok(adminPaymentService.findAllPayments(pageable));
    }

    /**
     * Retrieves the full details of a specific payment by its ID.
     * @param paymentId The ID of the payment.
     * @return The full Payment entity.
     */
    @Operation(summary = "Get payment details by ID (Admin only)",
               description = "Retrieves the full details of a specific payment by its ID. Requires ADMIN role.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                       @ApiResponse(responseCode = "404", description = "Payment not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailsDto> getPaymentById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(adminPaymentService.findPaymentById(paymentId));
    }

    /**
     * Get all pending refund requests (Admin only)
     */
    @Operation(summary = "Get all refund requests (Admin only)",
               description = "Retrieves all refund requests for admin review. Requires ADMIN role.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Refund requests retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/refund-requests")
    public ResponseEntity<Page<RefundDetailsDto>> getRefundRequests(Pageable pageable) {
        return ResponseEntity.ok(refundService.getRefundRequests(pageable));
    }

    /**
     * Approve and process a refund request (Admin only)
     */
    @Operation(summary = "Approve and process refund request (Admin only)",
               description = "Approves a refund request and processes the actual refund. Requires ADMIN role.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Refund approved and processed successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid refund request"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                       @ApiResponse(responseCode = "404", description = "Refund request not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/refund-requests/{refundId}/approve")
    public ResponseEntity<RefundResponseDto> approveRefund(@PathVariable Long refundId) {
        return ResponseEntity.ok(refundService.approveAndProcessRefund(refundId));
    }

    /**
     * Decline a refund request (Admin only)
     */
    @Operation(summary = "Decline refund request (Admin only)",
               description = "Declines a refund request. Requires ADMIN role.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Refund request declined successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid request"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                       @ApiResponse(responseCode = "404", description = "Refund request not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/refund-requests/{refundId}/decline")
    public ResponseEntity<RefundResponseDto> declineRefund(
            @PathVariable Long refundId,
            @RequestParam String adminNotes) {
        return ResponseEntity.ok(refundService.declineRefund(refundId, adminNotes));
    }

    /**
     * Manually triggers a refund for a specific payment.
     * This provides an administrative override or manual refund capability.
     * @param requestDto The refund request details.
     * @return The response from the refund processing.
     */
    @Operation(summary = "Process a manual refund (Admin only)",
               description = "Initiates a manual refund for a payment. Requires ADMIN role.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid refund request or payment not eligible for refund"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                       @ApiResponse(responseCode = "404", description = "Payment not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/refund")
    public ResponseEntity<RefundResponseDto> manualRefund(@Valid @RequestBody RefundRequestDto requestDto) {
        // This endpoint reuses the existing RefundService but is secured for admins.
        return ResponseEntity.ok(refundService.processRefund(requestDto));
    }

    /**
     * Retrieves all refunds associated with a specific payment.
     * @param paymentId The ID of the payment.
     * @return A list of Refund entities.
     */
    @Operation(summary = "Get refunds for a payment (Admin only)",
               description = "Retrieves a list of all refunds associated with a specific payment. Requires ADMIN role.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Refunds retrieved successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                       @ApiResponse(responseCode = "404", description = "Payment not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping("/{paymentId}/refunds")
    public ResponseEntity<List<RefundDetailsDto>> getRefundsForPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(adminPaymentService.findRefundsByPaymentId(paymentId));
    }
}
