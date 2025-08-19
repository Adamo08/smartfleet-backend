package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.common.SecurityUtilsService;
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

        PaymentProvider provider = providerFactory.getProvider(payment.getProvider())
                .orElseThrow(() -> new PaymentException("Provider not found for payment: " + payment.getId()));

        RefundResponseDto response = provider.processRefund(requestDto);
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
        // Fetch refunds via payments owned by user; simple approach: query all refunds and filter by ownership
        // For efficiency, consider a custom query. Here we map and filter.
        return refundRepository.findAll(pageable)
                .map(refund -> {
                    if (!refund.getPayment().getReservation().getUser().getId().equals(currentUser.getId())
                            && currentUser.getRole() != Role.ADMIN) {
                        throw new AccessDeniedException("You do not own this refund record.");
                    }
                    return refundMapper.toRefundDetailsDto(refund);
                });
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