package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationNotFoundException;
import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.payments.PaymentFilter;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProviderFactory providerFactory;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SecurityUtilsService securityUtilsService;
    private final Cache<String, PaymentResponseDto> idempotencyCache;
    private final PaymentMapper paymentMapper;
    private final com.adamo.vrspfab.notifications.NotificationService notificationService;
    private final com.adamo.vrspfab.notifications.NotificationType notificationType = com.adamo.vrspfab.notifications.NotificationType.GENERAL_UPDATE;

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
        // Notify user about payment outcome
        try {
            Reservation reservation = reservationRepository.findById(requestDto.getReservationId())
                    .orElseThrow(() -> new ReservationNotFoundException(requestDto.getReservationId()));
            var user = reservation.getUser();
            java.util.Map<String, Object> model = new java.util.HashMap<>();
            model.put("reservationId", reservation.getId());
            model.put("amount", requestDto.getAmount());
            model.put("currency", requestDto.getCurrency());
            if (response.getStatus() != null && response.getStatus().equalsIgnoreCase("COMPLETED")) {
                notificationService.createAndDispatchNotification(
                        user,
                        com.adamo.vrspfab.notifications.NotificationType.PAYMENT_SUCCESS,
                        "Payment completed for reservation #" + reservation.getId() + 
                                " (" + requestDto.getAmount() + " " + requestDto.getCurrency() + ")",
                        model
                );
            } else if (response.getStatus() != null && response.getStatus().equalsIgnoreCase("FAILED")) {
                notificationService.createAndDispatchNotification(
                        user,
                        com.adamo.vrspfab.notifications.NotificationType.PAYMENT_FAILURE,
                        "Payment failed for reservation #" + reservation.getId(),
                        model
                );
            }
        } catch (Exception ignored) {
            // Do not block payment flow on notification issues
        }
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyCache.put(idempotencyKey, response);
        }
        return response;
    }

    @Transactional
    public PaymentResponseDto confirmPayment(String sessionId) {
        // For providers like PayPal, sessionId corresponds to the order ID
        Payment payment = paymentRepository.findByTransactionId(sessionId)
                .orElseThrow(() -> new PaymentException("No pending payment found for session: " + sessionId));
        validatePaymentOwnership(payment.getId());

        PaymentProvider provider = providerFactory.getProvider(payment.getProvider())
                .orElseThrow(() -> new PaymentException("Provider not found for payment: " + payment.getId()));
        // Reuse processPayment semantics to capture
        PaymentRequestDto request = new PaymentRequestDto();
        request.setReservationId(payment.getReservation().getId());
        request.setAmount(payment.getAmount());
        request.setCurrency(payment.getCurrency());
        request.setPaymentMethodId(sessionId);
        request.setProviderName(provider.getProviderName());
        PaymentResponseDto response = provider.processPayment(request);
        try {
            var user = payment.getReservation().getUser();
            java.util.Map<String, Object> model = new java.util.HashMap<>();
            model.put("reservationId", payment.getReservation().getId());
            model.put("amount", payment.getAmount());
            model.put("currency", payment.getCurrency());
            if (response.getStatus() != null && response.getStatus().equalsIgnoreCase("COMPLETED")) {
                notificationService.createAndDispatchNotification(
                        user,
                        com.adamo.vrspfab.notifications.NotificationType.PAYMENT_SUCCESS,
                        "Payment completed for reservation #" + payment.getReservation().getId() + 
                                " (" + payment.getAmount() + " " + payment.getCurrency() + ")",
                        model
                );
            } else if (response.getStatus() != null && response.getStatus().equalsIgnoreCase("FAILED")) {
                notificationService.createAndDispatchNotification(
                        user,
                        com.adamo.vrspfab.notifications.NotificationType.PAYMENT_FAILURE,
                        "Payment failed for reservation #" + payment.getReservation().getId(),
                        model
                );
            }
        } catch (Exception ignored) {}
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

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByReservationId(Long reservationId) {
        validateOwnership(reservationId);
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new PaymentException("Payment not found for reservation ID: " + reservationId));
        return paymentMapper.toPaymentDto(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long paymentId) {
        validatePaymentOwnership(paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        return paymentMapper.toPaymentDto(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getUserPaymentHistory(Pageable pageable) {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        Page<Payment> payments = paymentRepository.findByReservationUserId(currentUser.getId(), pageable);
        return payments.map(paymentMapper::toPaymentDto);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getAllPaymentsAdmin(PaymentFilter filter, Pageable pageable) {
        PaymentSpecification spec = new PaymentSpecification(
                filter.getUserId(),
                filter.getReservationId(),
                filter.getStatus(),
                filter.getMinAmount(),
                filter.getMaxAmount(),
                filter.getStartDate(),
                filter.getEndDate()
        );
        Page<Payment> payments = paymentRepository.findAll(spec, pageable);
        return payments.map(paymentMapper::toPaymentDto);
    }

    @Transactional
    public void cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        validatePaymentOwnership(payment.getId());
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot cancel a completed payment");
        }
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        try {
            var user = payment.getReservation().getUser();
            java.util.Map<String, Object> model = new java.util.HashMap<>();
            model.put("reservationId", payment.getReservation().getId());
            notificationService.createAndDispatchNotification(
                    user,
                    com.adamo.vrspfab.notifications.NotificationType.PAYMENT_FAILURE,
                    "Payment for reservation #" + payment.getReservation().getId() + " has been cancelled.",
                    model
            );
        } catch (Exception ignored) {}
    }

    @Transactional
    public void deletePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        // Additional checks could be added here, e.g., only allow deletion of certain statuses
        paymentRepository.delete(payment);
    }

    @Transactional(readOnly = true)
    public PaymentStatsDto getPaymentStats() {
        User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
        
        // Get all payments for the current user
        List<Payment> userPayments = paymentRepository.findByReservationUserId(currentUser.getId());
        
        int totalPayments = userPayments.size();
        BigDecimal totalAmount = userPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int pendingPayments = (int) userPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();
        
        int completedPayments = (int) userPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();
        
        int failedPayments = (int) userPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .count();
        
        return PaymentStatsDto.builder()
                .totalPayments(totalPayments)
                .totalAmount(totalAmount)
                .pendingPayments(pendingPayments)
                .completedPayments(completedPayments)
                .failedPayments(failedPayments)
                .build();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        return paymentRepository.sumAmountByStatus(PaymentStatus.COMPLETED).orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public PaymentMethodsDto getPaymentMethods() {
        List<PaymentMethodsDto.PaymentMethodDto> methods = List.of(
                PaymentMethodsDto.PaymentMethodDto.builder()
                        .id("paypal")
                        .name("PayPal")
                        .description("Pay with your PayPal account")
                        .icon("paypal")
                        .isActive(true)
                        .build(),
                PaymentMethodsDto.PaymentMethodDto.builder()
                        .id("cmi")
                        .name("CMI")
                        .description("Pay securely with your card via CMI")
                        .icon("credit-card")
                        .isActive(true)
                        .build(),
                PaymentMethodsDto.PaymentMethodDto.builder()
                        .id("onsite")
                        .name("On-site Payment")
                        .description("Pay in person at our location")
                        .icon("store")
                        .isActive(true)
                        .build()
        );

        return PaymentMethodsDto.builder()
                .methods(methods)
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentMethodValidationDto validatePaymentMethod(String methodId) {
        boolean isValid = List.of("paypal", "cmi", "onsite").contains(methodId);
        String message = isValid ? "Payment method is valid" : "Payment method not supported";
        
        return PaymentMethodValidationDto.builder()
                .isValid(isValid)
                .message(message)
                .build();
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
