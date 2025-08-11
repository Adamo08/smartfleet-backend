package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationNotFoundException;
import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProviderFactory providerFactory;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SecurityUtilsService securityUtilsService;
    private final Cache<String, PaymentResponseDto> idempotencyCache;

    @Transactional
    public SessionResponseDto createPaymentSession(SessionRequestDto requestDto) {
        validateOwnership(requestDto.getReservationId());
        PaymentProvider provider = providerFactory.getProvider(requestDto.getProviderName())
                .orElseThrow(() -> new PaymentException("Invalid payment provider: " + requestDto.getProviderName()));
        return provider.createPaymentSession(requestDto);
    }

    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto, String idempotencyKey) {
        validateOwnership(requestDto.getReservationId());
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            PaymentResponseDto cached = idempotencyCache.asMap().get(idempotencyKey);
            if (cached != null) {
                return cached;
            }
        }
        PaymentProvider provider = providerFactory.getProvider(requestDto.getProviderName())
                .orElseThrow(() -> new PaymentException("Invalid payment provider: " + requestDto.getProviderName()));
        PaymentResponseDto response = provider.processPayment(requestDto);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyCache.put(idempotencyKey, response);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        // Validate ownership of the payment
        validatePaymentOwnership(payment.getId());
        // Assuming the provider name is stored on the payment entity
        PaymentProvider provider = providerFactory.getProvider(payment.getProvider())
                .orElseThrow(() -> new PaymentException("Provider not found for payment: " + paymentId));

        return provider.getPaymentStatus(paymentId);
    }



    /**
     * Validates that the current user owns the reservation associated with the payment.
     *
     * @param reservationId The ID of the reservation to validate ownership for.
     * @throws AccessDeniedException if the user does not own the reservation.
     */
    private void validateOwnership(Long reservationId) {
        // Get the current user
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();

        // Get the reservation by ID
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        // Check if the user is an admin or the owner of the reservation
        if (!reservation.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
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
}
