package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.dashboard.ActivityEventListener;
import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationNotFoundException;
import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentProviderFactory providerFactory;
    private final RefundRepository refundRepository;
    private final RefundMapper refundMapper;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SecurityUtilsService securityUtilsService;
    private final ActivityEventListener activityEventListener;
    private final com.adamo.vrspfab.notifications.NotificationService notificationService;

    /**
     * Processes a refund for a payment after validating ownership and input.
     *
     * @param requestDto The refund request details.
     * @return The refund response DTO.
     */
    @Transactional
    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
        validateRefundRequest(requestDto);
        Payment payment = paymentRepository.findWithDetailsById(requestDto.getPaymentId())
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + requestDto.getPaymentId()));

        // validate ownership of the payment
        validatePaymentOwnership(payment.getId());

        // Create refund request (not process immediately)
        Refund refund = createRefundRequest(requestDto, payment);
        
        // Return response indicating refund request was created
        return new RefundResponseDto(
                refund.getId(),
                null, // No transaction ID yet since it's just a request
                RefundStatus.REQUESTED
        );
    }

    @Transactional
    public RefundResponseDto approveAndProcessRefund(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentException("Refund not found with ID: " + refundId));

        // Only admins can approve refunds
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new PaymentException("Only administrators can approve refunds");
        }

        // Update refund status to PENDING (being processed)
        refund.setStatus(RefundStatus.PENDING);
        refundRepository.save(refund);

        Payment payment = refund.getPayment();
        PaymentProvider provider = providerFactory.getProvider(payment.getProvider())
                .orElseThrow(() -> new PaymentException("Provider not found for payment: " + payment.getId()));

        // Now process the actual refund
        RefundRequestDto requestDto = new RefundRequestDto();
        requestDto.setPaymentId(payment.getId());
        requestDto.setAmount(refund.getAmount());
        requestDto.setReason(refund.getReason());
        requestDto.setRefundMethod(refund.getRefundMethod());
        requestDto.setAdditionalNotes(refund.getAdditionalNotes());
        requestDto.setContactEmail(refund.getContactEmail());
        requestDto.setContactPhone(refund.getContactPhone());

        RefundResponseDto response = provider.processRefund(requestDto);
        
        // Record refund activity
        try {
            String description = String.format("Refund of %s %s processed for payment #%d", 
                    requestDto.getAmount(), payment.getCurrency(), payment.getId());
            String title = response.getStatus() == RefundStatus.PROCESSED ? "Refund Processed" : "Refund Failed";
            
            java.util.Map<String, Object> metadata = java.util.Map.of(
                    "refundId", response.getRefundRecordId(),
                    "paymentId", payment.getId(),
                    "amount", requestDto.getAmount(),
                    "currency", payment.getCurrency(),
                    "reason", requestDto.getReason() != null ? requestDto.getReason().getDescription() : "No reason provided",
                    "provider", payment.getProvider()
            );
            
            activityEventListener.recordRefundActivity(
                    title,
                    description,
                    currentUser,
                    response.getRefundRecordId(),
                    metadata
            );
        } catch (Exception e) {
            log.warn("Could not record refund activity: {}", e.getMessage());
        }
        
        // Notify user of refund outcome (non-blocking)
        try {
            var user = payment.getReservation().getUser();
            if (response.getStatus() == RefundStatus.PROCESSED) {
                notificationService.createAndDispatchNotification(
                        user,
                        com.adamo.vrspfab.notifications.NotificationType.REFUND_ISSUED,
                        "Refund processed for payment #" + payment.getId() + " on reservation #" + payment.getReservation().getId(),
                        java.util.Map.of(
                                "paymentId", payment.getId(),
                                "reservationId", payment.getReservation().getId(),
                                "amount", requestDto.getAmount(),
                                "currency", payment.getCurrency()
                        )
                );
            } else if (response.getStatus() == RefundStatus.FAILED) {
                notificationService.createAndDispatchNotification(
                        user,
                        com.adamo.vrspfab.notifications.NotificationType.SYSTEM_ALERT,
                        "Refund failed for payment #" + payment.getId(),
                        java.util.Map.of("paymentId", payment.getId())
                );
            }
        } catch (Exception ignored) {}
        return response;
    }

    @Transactional
    public RefundResponseDto declineRefund(Long refundId, String adminNotes) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentException("Refund not found with ID: " + refundId));

        // Only admins can decline refunds
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new PaymentException("Only administrators can decline refunds");
        }

        // Update refund status to DECLINED
        refund.setStatus(RefundStatus.DECLINED);
        refund.setAdditionalNotes(adminNotes);
        refundRepository.save(refund);

        // Notify user that refund was declined
        try {
            var user = refund.getPayment().getReservation().getUser();
            notificationService.createAndDispatchNotification(
                    user,
                    com.adamo.vrspfab.notifications.NotificationType.SYSTEM_ALERT,
                    "Your refund request for payment #" + refund.getPayment().getId() + " has been declined.",
                    java.util.Map.of(
                            "paymentId", refund.getPayment().getId(),
                            "refundId", refund.getId(),
                            "adminNotes", adminNotes
                    )
            );
        } catch (Exception ignored) {}

        return new RefundResponseDto(
                refund.getId(),
                null, // No transaction ID for declined refunds
                RefundStatus.DECLINED
        );
    }

    private Refund createRefundRequest(RefundRequestDto requestDto, Payment payment) {
        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setAmount(requestDto.getAmount());
        refund.setCurrency(payment.getCurrency());
        refund.setReason(requestDto.getReason());
        refund.setRefundMethod(requestDto.getRefundMethod());
        refund.setAdditionalNotes(requestDto.getAdditionalNotes());
        refund.setContactEmail(requestDto.getContactEmail());
        refund.setContactPhone(requestDto.getContactPhone());
        refund.setStatus(RefundStatus.REQUESTED);
        
        refund = refundRepository.save(refund);

        // Notify admins about new refund request
        try {
            notificationService.notifyAllAdmins(
                    com.adamo.vrspfab.notifications.NotificationType.REFUND_REQUEST,
                    "New refund request for payment #" + payment.getId(),
                    java.util.Map.of(
                            "paymentId", payment.getId(),
                            "refundId", refund.getId(),
                            "amount", requestDto.getAmount(),
                            "currency", payment.getCurrency(),
                            "reason", requestDto.getReason().getDescription()
                    )
            );
        } catch (Exception ignored) {}

        return refund;
    }

    /**
     * Retrieves refund details after validating ownership.
     *
     * @param refundId The ID of the refund.
     * @return The refund entity.
     */
    @Transactional(readOnly = true)
    public RefundDetailsDto getRefundDetails(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentException("Refund not found: " + refundId));

        validateOwnership(refund.getPayment().getReservation().getId());

        return refundMapper.toRefundDetailsDto(refund);
    }

    @Transactional(readOnly = true)
    public Page<RefundDetailsDto> getRefundHistory(Pageable pageable) {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        
        if (currentUser.getRole() == Role.ADMIN) {
            // Admins can see all refunds
            return refundRepository.findAll(pageable)
                    .map(refundMapper::toRefundDetailsDto);
        } else {
            // Regular users only see their own refunds
            return refundRepository.findByUserId(currentUser.getId(), pageable)
                    .map(refundMapper::toRefundDetailsDto);
        }
    }

    @Transactional(readOnly = true)
    public Page<RefundDetailsDto> getRefundRequests(Pageable pageable) {
        // Only admins can access refund requests
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new PaymentException("Only administrators can view refund requests");
        }
        
        // Get only REQUESTED refunds for admin review
        return refundRepository.findByStatus(RefundStatus.REQUESTED, pageable)
                .map(refundMapper::toRefundDetailsDto);
    }

    @Transactional(readOnly = true)
    public Page<RefundDetailsDto> getRefundHistoryWithFilters(
            Pageable pageable, 
            Long paymentId, 
            String status,
            java.math.BigDecimal minAmount,
            java.math.BigDecimal maxAmount,
            String startDate,
            String endDate,
            String searchTerm) {
        
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        
        // Only admins can use filters for now
        if (currentUser.getRole() != Role.ADMIN) {
            return getRefundHistory(pageable);
        }
        
        RefundStatus refundStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                refundStatus = RefundStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (startDate != null && !startDate.isEmpty()) {
            try {
                startDateTime = LocalDate.parse(startDate).atStartOfDay();
            } catch (Exception e) {
                // Invalid date, ignore
            }
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            try {
                endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
            } catch (Exception e) {
                // Invalid date, ignore  
            }
        }
        
        return refundRepository.findAllWithFilters(
                paymentId,
                refundStatus,
                minAmount,
                maxAmount,
                startDateTime,
                endDateTime,
                searchTerm,
                pageable
        ).map(refundMapper::toRefundDetailsDto);
    }


    /**
     * Validates that the current user owns the reservation associated with the refund.
     *
     * @param reservationId The ID of the reservation to validate ownership for.
     * @throws AccessDeniedException if the user does not own the reservation.
     */
    private void validateOwnership(Long reservationId) {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (!reservation.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            log.warn("Access denied for user {} on reservation {}", currentUser.getId(), reservationId);
            throw new AccessDeniedException("You do not own this reservation.");
        }
    }


    /**
     * Validates that the current user owns the payment.
     *
     * @param paymentId The ID of the payment to validate ownership for.
     * @throws AccessDeniedException if the user does not own the payment.
     */
    private void validatePaymentOwnership(Long paymentId) {
        // Get the current user
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();

        // Get the payment by ID
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));

        // Check if the user is an admin or the owner of the reservation associated with the payment
        if (!payment.getReservation().getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not own this payment.");
        }

    }

    /**
     * Validates the refund request DTO.
     *
     * @param requestDto The refund request details.
     * @throws IllegalArgumentException if the amount is not positive.
     */
    private void validateRefundRequest(RefundRequestDto requestDto) {
        if (requestDto.getAmount() == null || requestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive.");
        }
    }
}