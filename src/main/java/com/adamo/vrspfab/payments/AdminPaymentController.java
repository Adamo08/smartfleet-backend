package com.adamo.vrspfab.payments;

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
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;
    private final RefundService refundService;

    /**
     * Retrieves a paginated list of all payments.
     * @param pageable Pagination information.
     * @return A page of Payment entities.
     */
    @GetMapping
    public ResponseEntity<Page<Payment>> getAllPayments(Pageable pageable) {
        return ResponseEntity.ok(adminPaymentService.findAllPayments(pageable));
    }

    /**
     * Retrieves the full details of a specific payment by its ID.
     * @param paymentId The ID of the payment.
     * @return The full Payment entity.
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(adminPaymentService.findPaymentById(paymentId));
    }

    /**
     * Manually triggers a refund for a specific payment.
     * This provides an administrative override or manual refund capability.
     * @param requestDto The refund request details.
     * @return The response from the refund processing.
     */
    @PostMapping("/refund")
    public ResponseEntity<RefundResponseDto> manualRefund(@RequestBody RefundRequestDto requestDto) {
        // This endpoint reuses the existing RefundService but is secured for admins.
        return ResponseEntity.ok(refundService.processRefund(requestDto));
    }

    /**
     * Retrieves all refunds associated with a specific payment.
     * @param paymentId The ID of the payment.
     * @return A list of Refund entities.
     */
    @GetMapping("/{paymentId}/refunds")
    public ResponseEntity<List<Refund>> getRefundsForPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(adminPaymentService.findRefundsByPaymentId(paymentId));
    }
}
