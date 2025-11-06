package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.common.containers.MySqlTestBaseIT;
import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationRepository;
import com.adamo.vrspfab.reservations.ReservationStatus;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.users.UserRepository;
import com.adamo.vrspfab.users.AuthProvider;
import com.adamo.vrspfab.vehicles.Vehicle;
import com.adamo.vrspfab.vehicles.VehicleRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import com.adamo.vrspfab.common.config.TestMailConfig;
import com.adamo.vrspfab.common.config.IntegrationTestConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import({TestMailConfig.class, IntegrationTestConfig.class})
@TestPropertySource(properties = {
    "spring.profiles.active=test",
    "spring.main.allow-bean-definition-overriding=true"
})
class PaymentServiceFlowIT extends MySqlTestBaseIT {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    @Qualifier("paymentIdempotencyCache")
    private Cache<String, PaymentResponseDto> idempotencyCache;

    @MockBean
    private SecurityUtilsService securityUtilsService;

    @MockBean
    private PaymentProviderFactory providerFactory;

    private User testUser;
    private Reservation testReservation;
    private PaymentProvider stubProvider;

    @BeforeEach
    @Transactional
    void setup() {
        // Clean up before each test
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        idempotencyCache.invalidateAll();

        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .build();
        testUser = userRepository.save(testUser);

        // Use existing seeded vehicle (from Flyway migrations)
        Vehicle vehicle = vehicleRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No vehicles found in database. Ensure Flyway migrations are run."));

        // Create test reservation
        testReservation = Reservation.builder()
                .user(testUser)
                .vehicle(vehicle)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .status(ReservationStatus.PENDING)
                .build();
        testReservation = reservationRepository.save(testReservation);

        // Create stub payment provider
        stubProvider = new PaymentProvider() {
                @Override
                public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
                    // Create and persist payment
                    Reservation reservation = reservationRepository.findById(requestDto.getReservationId())
                            .orElseThrow(() -> new PaymentException("Reservation not found"));
                    
                    Payment payment = Payment.builder()
                            .reservation(reservation)
                            .amount(requestDto.getAmount())
                            .currency(requestDto.getCurrency())
                            .status(PaymentStatus.COMPLETED)
                            .transactionId("test-txn-" + System.currentTimeMillis())
                            .provider("testPaymentProvider")
                            .build();
                    Payment saved = paymentRepository.save(payment);
                    
                    return new PaymentResponseDto(
                            saved.getId(),
                            saved.getTransactionId(),
                            saved.getStatus().name(),
                            null
                    );
                }

                @Override
                public SessionResponseDto createPaymentSession(SessionRequestDto requestDto) {
                    return new SessionResponseDto(
                            "test-session-" + System.currentTimeMillis(),
                            "https://test-checkout.example.com"
                    );
                }

                @Override
                public PaymentResponseDto getPaymentStatus(Long paymentId) {
                    return new PaymentResponseDto(
                            paymentId,
                            "test-txn",
                            "COMPLETED",
                            null
                    );
                }

                @Override
                public RefundResponseDto processRefund(RefundRequestDto requestDto) {
                    return new RefundResponseDto(
                            1L,
                            "test-refund-" + System.currentTimeMillis(),
                            RefundStatus.PROCESSED
                    );
                }

                @Override
                public boolean canProcessPayment(Long reservationId) {
                    return true;
                }

                @Override
                public String getProviderName() {
                    return "testPaymentProvider";
                }
            };

        // Configure mock factory to return our stub provider
        when(providerFactory.getProvider("testPaymentProvider")).thenReturn(Optional.of(stubProvider));
        when(providerFactory.getProvider("INVALID_PROVIDER")).thenReturn(Optional.empty());
    }

    private void configureSecurityMock() {
        // Configure the mock to return test user after it's created
        if (testUser != null && securityUtilsService != null) {
            when(securityUtilsService.getCurrentAuthenticatedUser()).thenReturn(testUser);
        }
    }

    @Test
    @Transactional
    void createPaymentSession_whenValidRequest_createsSession() {
        // Arrange
        configureSecurityMock();
        SessionRequestDto request = new SessionRequestDto();
        request.setReservationId(testReservation.getId());
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setCurrency("USD");
        request.setSuccessUrl("https://example.com/success");
        request.setCancelUrl("https://example.com/cancel");
        request.setProviderName("testPaymentProvider");

        // Act
        SessionResponseDto response = paymentService.createPaymentSession(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getSessionId());
        assertNotNull(response.getCheckoutUrl());
        assertTrue(response.getSessionId().startsWith("test-session-"));
        assertTrue(response.getCheckoutUrl().contains("test-checkout"));
    }

    @Test
    @Transactional
    void createPaymentSession_whenInvalidProvider_throwsException() {
        // Arrange
        configureSecurityMock();
        SessionRequestDto request = new SessionRequestDto();
        request.setReservationId(testReservation.getId());
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setCurrency("USD");
        request.setSuccessUrl("https://example.com/success");
        request.setCancelUrl("https://example.com/cancel");
        request.setProviderName("INVALID_PROVIDER");

        // Act & Assert
        assertThrows(PaymentException.class, () -> paymentService.createPaymentSession(request));
    }

    @Test
    @Transactional
    void processPayment_whenValidRequest_processesPayment() {
        // Arrange
        configureSecurityMock();
        PaymentRequestDto request = new PaymentRequestDto();
        request.setReservationId(testReservation.getId());
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setCurrency("USD");
        request.setPaymentMethodId("test-method");
        request.setProviderName("testPaymentProvider");

        // Act
        PaymentResponseDto response = paymentService.processPayment(request, null);

        // Assert
        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertNotNull(response.getTransactionId());
        assertTrue(response.getTransactionId().startsWith("test-txn-"));

        // Verify payment was persisted
        Optional<Payment> savedPayment = paymentRepository.findByReservationId(testReservation.getId());
        assertTrue(savedPayment.isPresent());
        assertEquals(PaymentStatus.COMPLETED, savedPayment.get().getStatus());
    }

    @Test
    @Transactional
    void processPayment_whenIdempotencyKeyProvided_returnsCachedResult() {
        // Arrange
        configureSecurityMock();
        PaymentRequestDto request = new PaymentRequestDto();
        request.setReservationId(testReservation.getId());
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setCurrency("USD");
        request.setPaymentMethodId("test-method");
        request.setProviderName("testPaymentProvider");

        String idempotencyKey = "test-key-123";

        // Act - first call
        PaymentResponseDto firstResponse = paymentService.processPayment(request, idempotencyKey);

        // Act - second call with same key (should return cached)
        PaymentResponseDto secondResponse = paymentService.processPayment(request, idempotencyKey);

        // Assert - both should be the same (cached)
        assertNotNull(firstResponse);
        assertNotNull(secondResponse);
        assertEquals(firstResponse.getPaymentId(), secondResponse.getPaymentId());
        assertEquals(firstResponse.getTransactionId(), secondResponse.getTransactionId());

        // Verify only one payment was created
        long paymentCount = paymentRepository.findAll().stream()
                .filter(p -> p.getReservation().getId().equals(testReservation.getId()))
                .count();
        assertEquals(1, paymentCount);
    }
}

