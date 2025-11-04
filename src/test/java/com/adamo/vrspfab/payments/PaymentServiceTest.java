package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationNotFoundException;
import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class PaymentServiceTest {

    @Mock private PaymentProviderFactory providerFactory;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private SecurityUtilsService securityUtilsService;
    @Mock private Cache<String, PaymentResponseDto> idempotencyCache;
    @Mock private PaymentMapper paymentMapper;
    @Mock private com.adamo.vrspfab.notifications.NotificationService notificationService;
    @Mock private PaymentProvider paymentProvider;

    @InjectMocks private PaymentService paymentService;

    private User currentUser;
    private Reservation reservation;

    @BeforeEach
    void setup() {
        currentUser = User.builder().id(1L).role(Role.CUSTOMER).build();
        User owner = User.builder().id(1L).role(Role.CUSTOMER).build();
        reservation = Reservation.builder().id(10L).user(owner).build();
        given(securityUtilsService.getCurrentAuthenticatedUser()).willReturn(currentUser);
    }

    @Test
    void createPaymentSession_whenInvalidProvider_throwsPaymentException() {
        SessionRequestDto request = new SessionRequestDto();
        request.setReservationId(10L);
        request.setProviderName("INVALID");
        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));
        given(providerFactory.getProvider("INVALID")).willReturn(Optional.empty());

        assertThrows(PaymentException.class, () -> paymentService.createPaymentSession(request));
    }

    @Test
    void createPaymentSession_whenReservationNotOwned_throwsAccessDenied() {
        User otherUser = User.builder().id(99L).role(Role.CUSTOMER).build();
        Reservation otherReservation = Reservation.builder().id(10L).user(otherUser).build();
        SessionRequestDto request = new SessionRequestDto();
        request.setReservationId(10L);
        given(reservationRepository.findById(10L)).willReturn(Optional.of(otherReservation));

        assertThrows(AccessDeniedException.class, () -> paymentService.createPaymentSession(request));
    }

    @Test
    void processPayment_whenIdempotencyKeyExists_returnsCached() {
        String idempotencyKey = "key-123";
        PaymentResponseDto cached = new PaymentResponseDto(1L, "tx-123", "COMPLETED", null);
        java.util.concurrent.ConcurrentMap<String, PaymentResponseDto> map = new java.util.concurrent.ConcurrentHashMap<>();
        map.put(idempotencyKey, cached);
        given(idempotencyCache.asMap()).willReturn(map);

        PaymentRequestDto request = new PaymentRequestDto();
        request.setReservationId(10L);
        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));

        PaymentResponseDto result = paymentService.processPayment(request, idempotencyKey);
        assertEquals(cached, result);
    }

    @Test
    void completeOnsitePayment_whenNotAdmin_throwsPaymentException() {
        User customer = User.builder().id(1L).role(Role.CUSTOMER).build();
        given(securityUtilsService.getCurrentAuthenticatedUser()).willReturn(customer);

        assertThrows(PaymentException.class, () -> paymentService.completeOnsitePayment(10L, "notes"));
    }

    @Test
    void getPaymentStatus_whenPaymentNotFound_throwsPaymentException() {
        given(paymentRepository.findById(100L)).willReturn(Optional.empty());

        assertThrows(PaymentException.class, () -> paymentService.getPaymentStatus(100L));
    }

    @Test
    void getPaymentStatus_whenPaymentNotOwned_throwsAccessDenied() {
        User otherUser = User.builder().id(99L).role(Role.CUSTOMER).build();
        Reservation otherReservation = Reservation.builder().id(10L).user(otherUser).build();
        Payment payment = Payment.builder().id(100L).reservation(otherReservation).build();
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment));

        assertThrows(AccessDeniedException.class, () -> paymentService.getPaymentStatus(100L));
    }
}

