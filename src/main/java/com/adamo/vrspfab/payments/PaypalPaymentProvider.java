package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.reservations.ReservationRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A modern, production-ready implementation of the PaymentProvider for PayPal.
 * This class uses direct REST API calls to the PayPal v2 API via RestTemplate,
 * avoiding the deprecated PayPal SDK.
 */
@Slf4j
@RequiredArgsConstructor
@Service("paypalPaymentProvider")
public class PaypalPaymentProvider implements PaymentProvider {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RefundRepository refundRepository;
    private final RestTemplate restTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${paypal.api.baseUrl}")
    private String paypalBaseUrl;

    @Value("${paypal.client.id}")
    private String paypalClientId;

    @Value("${paypal.client.secret}")
    private String paypalClientSecret;

    // A simple in-memory cache for the access token. For multi-instance deployments, we will use a distributed cache like Redis.
    private volatile String accessToken;
    private volatile LocalDateTime tokenExpiryTime;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        String orderId = requestDto.getPaymentMethodId();  // Repurpose as PayPal order ID
        if (orderId == null) {
            throw new PaymentException("PaymentMethodId (Order ID) is required for PayPal capture.");
        }

        Payment payment = paymentRepository.findByTransactionId(orderId)
                .orElseThrow(() -> new PaymentException("No pending payment found for Order ID: " + orderId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException("Cannot capture non-pending payment. Status: " + payment.getStatus());
        }

        // Verify amounts match to prevent tampering
        if (!payment.getAmount().equals(requestDto.getAmount()) || !payment.getCurrency().equals(requestDto.getCurrency())) {
            throw new PaymentException("Request amount/currency does not match pending payment.");
        }

        try {
            String token = getAccessToken();
            HttpHeaders headers = createAuthHeaders(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<PaypalOrderResponse> response = restTemplate.postForEntity(
                    paypalBaseUrl + "/v2/checkout/orders/" + orderId + "/capture",
                    entity,
                    PaypalOrderResponse.class
            );

            PaypalOrderResponse capturedOrder = Objects.requireNonNull(response.getBody());
            if (!"COMPLETED".equalsIgnoreCase(capturedOrder.getStatus())) {
                throw new PaymentException("PayPal order capture failed. Status: " + capturedOrder.getStatus());
            }

            // Extract capture ID (optional, but useful for refunds)
            String captureId = capturedOrder.getPurchaseUnits().stream()
                    .findFirst()
                    .flatMap(pu -> pu.getPayments().getCaptures().stream().findFirst())
                    .map(PaypalCapture::getId)
                    .orElse(null);

            // Update payment
            payment.setStatus(PaymentStatus.COMPLETED);
            if (captureId != null) {
                payment.setTransactionId(captureId);  // Update to capture ID for future refs/refunds
                payment.setCaptureId(captureId);
            }
            paymentRepository.save(payment);

            // Fire payment completed event to trigger business logic
            applicationEventPublisher.publishEvent(new PaymentCompletedEvent(
                    this, 
                    payment.getId(), 
                    payment.getReservation().getId(), 
                    captureId != null ? captureId : orderId, 
                    PaymentStatus.COMPLETED
            ));

            return new PaymentResponseDto(payment.getId(), capturedOrder.getId(), "COMPLETED", null);

        } catch (HttpClientErrorException e) {
            log.error("PayPal capture error for order {}: {} - {}", orderId, e.getStatusCode(), e.getResponseBodyAsString());
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentException("PayPal capture failed: " + e.getResponseBodyAsString(), e);
        }
    }

    @Override
    @Transactional
    public SessionResponseDto createPaymentSession(SessionRequestDto requestDto) {
        log.info("=== PAYPAL SESSION CREATION STARTED ===");
        log.info("Reservation ID: {}", requestDto.getReservationId());
        log.info("Amount: {} {}", requestDto.getAmount(), requestDto.getCurrency());
        log.info("Success URL: {}", requestDto.getSuccessUrl());
        log.info("Cancel URL: {}", requestDto.getCancelUrl());
        
        try {
            String token = getAccessToken();
            log.info("PayPal access token obtained successfully");
            
            Payment payment = createPendingPayment(requestDto);
            log.info("Pending payment created with ID: {}", payment.getId());

            // 1. Create PayPal Order
            HttpHeaders headers = createAuthHeaders(token);
            PaypalOrderRequest orderRequest = createOrderPayload(requestDto, payment.getId());
            HttpEntity<PaypalOrderRequest> entity = new HttpEntity<>(orderRequest, headers);

            log.info("Creating PayPal order...");
            ResponseEntity<PaypalOrderResponse> response = restTemplate.postForEntity(
                    paypalBaseUrl + "/v2/checkout/orders",
                    entity,
                    PaypalOrderResponse.class
            );

            PaypalOrderResponse orderResponse = response.getBody();
            if (orderResponse == null || orderResponse.getId() == null) {
                throw new PaymentException("Failed to create PayPal order: Empty response from PayPal.");
            }

            log.info("PayPal order created successfully with ID: {}", orderResponse.getId());

            // 2. Find Approval Link
            String approvalUrl = orderResponse.getLinks().stream()
                    .filter(link -> "approve".equals(link.getRel()))
                    .map(PaypalLink::getHref)
                    .findFirst()
                    .orElseThrow(() -> new PaymentException("No approval URL found in PayPal response."));

            log.info("PayPal approval URL found: {}", approvalUrl);

            // 3. Update our payment record with the PayPal Order ID
            payment.setTransactionId(orderResponse.getId());
            paymentRepository.save(payment);
            log.info("Payment record updated with PayPal Order ID: {}", orderResponse.getId());

            // Return our internal payment ID as sessionId and PayPal's approval URL
            // This allows us to track the payment session and handle redirects properly
            SessionResponseDto sessionResponse = new SessionResponseDto(payment.getId().toString(), approvalUrl);
            log.info("=== PAYPAL SESSION CREATION COMPLETED ===");
            log.info("Session ID: {}, Redirect URL: {}", sessionResponse.getSessionId(), sessionResponse.getCheckoutUrl());
            return sessionResponse;

        } catch (HttpClientErrorException e) {
            log.error("PayPal API error during session creation: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException("PayPal API error: " + e.getResponseBodyAsString(), e);
        }
    }


    @Override
    @Transactional
    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
        // 1. Find the original payment record
        Payment payment = paymentRepository.findById(requestDto.getPaymentId())
                .orElseThrow(() -> new PaymentException("Cannot refund non-existent payment with ID: " + requestDto.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot refund a payment that is not completed. Status: " + payment.getStatus());
        }

        String captureId = payment.getCaptureId();
        if (captureId == null || captureId.isBlank()) {
            throw new PaymentException("Payment " + payment.getId() + " has no Capture ID to refund.");
        }

        // 2. CREATE AND SAVE a PENDING refund record FIRST
        // This records our intent to refund before we make the external call.
        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setAmount(requestDto.getAmount());
        refund.setCurrency(payment.getCurrency());
        refund.setReason(requestDto.getReason());
        // Populate additional audit/contact fields
        refund.setRefundMethod(requestDto.getRefundMethod() != null ? requestDto.getRefundMethod() : "PAYPAL");
        refund.setAdditionalNotes(requestDto.getAdditionalNotes());
        refund.setContactEmail(requestDto.getContactEmail());
        refund.setContactPhone(requestDto.getContactPhone());
        refund.setStatus(RefundStatus.PENDING); // Mark as PENDING
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);

        try {
            String token = getAccessToken();
            HttpHeaders headers = createAuthHeaders(token);

            PaypalRefundRequest refundRequest = new PaypalRefundRequest(
                    new PaypalMoney(payment.getCurrency(), String.valueOf(requestDto.getAmount()))
            );
            HttpEntity<PaypalRefundRequest> entity = new HttpEntity<>(refundRequest, headers);

            // 3. EXECUTE the external API call
            ResponseEntity<PaypalRefundResponse> response = restTemplate.postForEntity(
                    paypalBaseUrl + "/v2/payments/captures/" + captureId + "/refund",
                    entity,
                    PaypalRefundResponse.class
            );

            PaypalRefundResponse refundResponse = response.getBody();
            if (refundResponse == null || !"COMPLETED".equalsIgnoreCase(refundResponse.getStatus())) {
                throw new PaymentException("PayPal refund was not completed successfully. Status: " +
                        (refundResponse != null ? refundResponse.getStatus() : "UNKNOWN"));
            }

            // 4. SUCCESS: Update the record with the outcome
            refund.setStatus(RefundStatus.PROCESSED);
            refund.setRefundTransactionId(refundResponse.getId());
            payment.setStatus(PaymentStatus.REFUNDED);

            return new RefundResponseDto(refund.getId(), refund.getRefundTransactionId(), refund.getStatus());

        } catch (HttpClientErrorException e) {
            // 5. FAILURE: Update the record with the failure details
            log.error("PayPal API error during refund for capture ID {}: {} - {}", captureId, e.getStatusCode(), e.getResponseBodyAsString());
            refund.setStatus(RefundStatus.FAILED); // Mark our internal refund record as FAILED
            // We might want to add a field to the Refund entity to store the error message
            // refund.setFailureReason(e.getResponseBodyAsString());
            throw new PaymentException("PayPal API error during refund: " + e.getResponseBodyAsString(), e);
        } finally {
            // 6. ALWAYS save the final state of the payment and refund records
            paymentRepository.save(payment);
            refundRepository.save(refund);
        }
    }


//    @Override
//    @Transactional
//    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
//        // Find the original payment record in your database
//        Payment payment = paymentRepository.findById(requestDto.getPaymentId())
//                .orElseThrow(() -> new PaymentException("Cannot refund non-existent payment with ID: " + requestDto.getPaymentId()));
//
//        // Ensure the payment is in a refundable state
//        if (payment.getStatus() != PaymentStatus.COMPLETED) {
//            throw new PaymentException("Cannot refund a payment that is not completed. Status: " + payment.getStatus());
//        }
//
//        // Get the capture ID that was stored when the payment was completed.
//        // This is the critical fix: use the stored ID directly.
//        String captureId = payment.getCaptureId();
//        if (captureId == null || captureId.isBlank()) {
//            throw new PaymentException("Cannot refund payment " + payment.getId() + " because it has no associated Capture ID.");
//        }
//
//        try {
//            // Get a valid PayPal API access token
//            String token = getAccessToken();
//            HttpHeaders headers = createAuthHeaders(token);
//
//            // Prepare the refund request payload for the PayPal API
//            PaypalRefundRequest refundRequest = new PaypalRefundRequest(
//                    new PaypalMoney(payment.getCurrency(), String.valueOf(requestDto.getAmount()))
//            );
//            HttpEntity<PaypalRefundRequest> entity = new HttpEntity<>(refundRequest, headers);
//
//            // Execute the refund request against the specific PayPal Capture ID
//            ResponseEntity<PaypalRefundResponse> response = restTemplate.postForEntity(
//                    paypalBaseUrl + "/v2/payments/captures/" + captureId + "/refund",
//                    entity,
//                    PaypalRefundResponse.class
//            );
//
//            PaypalRefundResponse refundResponse = response.getBody();
//            if (refundResponse == null || !"COMPLETED".equalsIgnoreCase(refundResponse.getStatus())) {
//                String status = (refundResponse != null) ? refundResponse.getStatus() : "UNKNOWN";
//                throw new PaymentException("PayPal refund was not completed successfully. Status: " + status);
//            }
//
//            // Create and save our internal refund record for tracking
//            Refund refund = new Refund();
//            refund.setPayment(payment);
//            refund.setRefundTransactionId(refundResponse.getId());
//            refund.setAmount(requestDto.getAmount());
//            refund.setCurrency(payment.getCurrency());
//            refund.setReason(requestDto.getReason());
//            refund.setStatus(RefundStatus.PROCESSED);
//            refund.setProcessedAt(LocalDateTime.now());
//            refundRepository.save(refund);
//
//            // Update the original payment's status to REFUNDED
//            payment.setStatus(PaymentStatus.REFUNDED);
//            paymentRepository.save(payment);
//
//            // Return a response DTO with details of our internal refund record
//            return new RefundResponseDto(refund.getId(), refund.getRefundTransactionId(), refund.getStatus());
//
//        } catch (HttpClientErrorException e) {
//            log.error("PayPal API error during refund for capture ID {}: {} - {}", captureId, e.getStatusCode(), e.getResponseBodyAsString());
//            // You could create a FAILED refund record here if desired for tracking purposes
//            throw new PaymentException("PayPal API error during refund: " + e.getResponseBodyAsString(), e);
//        }
//    }


//    @Override
//    @Transactional
//    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
//        Payment payment = paymentRepository.findById(requestDto.getPaymentId())
//                .orElseThrow(() -> new PaymentException("Cannot refund non-existent payment with ID: " + requestDto.getPaymentId()));
//
//        if (payment.getStatus() != PaymentStatus.COMPLETED) {
//            throw new PaymentException("Cannot refund a payment that is not completed. Status: " + payment.getStatus());
//        }
//
//        try {
//            String token = getAccessToken();
//            HttpHeaders headers = createAuthHeaders(token);
//
//            // 1. Get the Capture ID from the completed order
//            String captureId = getCaptureIdForOrder(payment.getTransactionId(), token);
//
//            // 2. Execute the refund against the Capture ID
//            PaypalRefundRequest refundRequest = new PaypalRefundRequest(
//                    new PaypalMoney(payment.getCurrency(), String.valueOf(requestDto.getAmount()))
//            );
//            HttpEntity<PaypalRefundRequest> entity = new HttpEntity<>(refundRequest, headers);
//
//            ResponseEntity<PaypalRefundResponse> response = restTemplate.postForEntity(
//                    paypalBaseUrl + "/v2/payments/captures/" + captureId + "/refund",
//                    entity,
//                    PaypalRefundResponse.class
//            );
//
//            PaypalRefundResponse refundResponse = response.getBody();
//            if (refundResponse == null || !"COMPLETED".equalsIgnoreCase(refundResponse.getStatus())) {
//                throw new PaymentException("PayPal refund was not completed successfully. Status: " + (refundResponse != null ? refundResponse.getStatus() : "UNKNOWN"));
//            }
//
//            // 3. Create and save our internal refund record
//            Refund refund = new Refund();
//            refund.setPayment(payment);
//            refund.setRefundTransactionId(refundResponse.getId());
//            refund.setAmount(requestDto.getAmount());
//            refund.setCurrency(payment.getCurrency());
//            refund.setReason(requestDto.getReason());
//            refund.setStatus(RefundStatus.PROCESSED);
//            refund.setProcessedAt(LocalDateTime.now());
//            refundRepository.save(refund);
//
//            payment.setStatus(PaymentStatus.REFUNDED);
//            paymentRepository.save(payment);
//
//            return new RefundResponseDto(refund.getId(), refund.getRefundTransactionId(), refund.getStatus());
//
//        } catch (HttpClientErrorException e) {
//            log.error("PayPal API error during refund: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
//            throw new PaymentException("PayPal API error during refund: " + e.getResponseBodyAsString(), e);
//        }
//    }



    @Override
    public String getProviderName() {
        return "paypalPaymentProvider";
    }

    @Override
    public PaymentResponseDto getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        if (payment.getTransactionId() == null) {
            throw new PaymentException("Payment has no transaction ID to check status.");
        }

        try {
            String token = getAccessToken();
            HttpHeaders headers = createAuthHeaders(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String endpoint;
            if (payment.getStatus() == PaymentStatus.COMPLETED && payment.getCaptureId() != null) {
                endpoint = paypalBaseUrl + "/v2/payments/captures/" + payment.getCaptureId();
            } else {
                endpoint = paypalBaseUrl + "/v2/checkout/orders/" + payment.getTransactionId();
            }

            ResponseEntity<PaypalOrderResponse> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    PaypalOrderResponse.class
            );

            PaypalOrderResponse order = Objects.requireNonNull(response.getBody());
            return new PaymentResponseDto(paymentId, order.getId(), order.getStatus(), null);

        } catch (HttpClientErrorException e) {
            log.error("PayPal API error during status check: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException("PayPal API error during status check: " + e.getResponseBodyAsString(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canProcessPayment(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .map(payment -> payment.getStatus() == PaymentStatus.PENDING)
                .orElse(false);
    }


    String getAccessToken() {
        if (accessToken == null || LocalDateTime.now().isAfter(tokenExpiryTime)) {
            synchronized (this) {
                if (accessToken == null || LocalDateTime.now().isAfter(tokenExpiryTime)) {
                    log.info("Fetching new PayPal access token.");
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBasicAuth(paypalClientId, paypalClientSecret);
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                    map.add("grant_type", "client_credentials");

                    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

                    try {
                        ResponseEntity<PaypalTokenResponse> response = restTemplate.postForEntity(
                                paypalBaseUrl + "/v1/oauth2/token",
                                entity,
                                PaypalTokenResponse.class
                        );

                        PaypalTokenResponse tokenResponse = Objects.requireNonNull(response.getBody());
                        this.accessToken = tokenResponse.getAccessToken();
                        this.tokenExpiryTime = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn() - 60); // 60s buffer
                    } catch (HttpClientErrorException e) {
                        log.error("Failed to get PayPal access token: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                        throw new PaymentException("Could not authenticate with PayPal.", e);
                    }
                }
            }
        }
        return this.accessToken;
    }

    private String getCaptureIdForOrder(String orderId, String token) {
        HttpHeaders headers = createAuthHeaders(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PaypalOrderResponse> response = restTemplate.exchange(
                paypalBaseUrl + "/v2/checkout/orders/" + orderId,
                HttpMethod.GET,
                entity,
                PaypalOrderResponse.class
        );

        return Optional.ofNullable(response.getBody())
                .map(PaypalOrderResponse::getPurchaseUnits)
                .flatMap(pus -> pus.stream().findFirst())
                .map(PaypalPurchaseUnit::getPayments)
                .map(PaypalPayments::getCaptures)
                .flatMap(caps -> caps.stream().findFirst())
                .map(PaypalCapture::getId)
                .orElseThrow(() -> new PaymentException("Could not find a valid capture ID for order " + orderId));
    }

    private Payment createPendingPayment(SessionRequestDto requestDto) {

        var reservation = reservationRepository.findById(requestDto.getReservationId())
                .orElseThrow(() -> new PaymentException("Reservation not found with ID: " + requestDto.getReservationId()));

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(requestDto.getAmount())
                .currency(requestDto.getCurrency())
                .status(PaymentStatus.PENDING)
                .provider("paypalPaymentProvider")
                .build();
        return paymentRepository.save(payment);
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private PaypalOrderRequest createOrderPayload(SessionRequestDto requestDto, Long internalPaymentId) {
        PaypalMoney amount = new PaypalMoney(requestDto.getCurrency(), requestDto.getAmount().toString());
        PaypalPurchaseUnit purchaseUnit = new PaypalPurchaseUnit(amount);
        // include internal reference for reconciliation
        purchaseUnit.setReferenceId(String.valueOf(internalPaymentId));

        // Use URLs provided by the client as-is (minus query params) to avoid duplicating path segments
        String providedSuccessUrl = requestDto.getSuccessUrl();
        String providedCancelUrl = requestDto.getCancelUrl();

        String successUrl = providedSuccessUrl != null ? providedSuccessUrl.split("\\?")[0] : null;
        String cancelUrl = providedCancelUrl != null ? providedCancelUrl.split("\\?")[0] : null;

        PaypalApplicationContext context = new PaypalApplicationContext(successUrl, cancelUrl);
        return new PaypalOrderRequest("CAPTURE", List.of(purchaseUnit), context);
    }

    // --- PayPal API DTOs (as private static inner classes for encapsulation) ---

    @Data
    private static class PaypalTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("expires_in")
        private long expiresIn;
    }

    @Data
    private static class PaypalOrderRequest {
        private String intent;
        @JsonProperty("purchase_units")
        private List<PaypalPurchaseUnit> purchaseUnits;
        @JsonProperty("application_context")
        private PaypalApplicationContext applicationContext;

        public PaypalOrderRequest(String intent, List<PaypalPurchaseUnit> purchaseUnits, PaypalApplicationContext applicationContext) {
            this.intent = intent;
            this.purchaseUnits = purchaseUnits;
            this.applicationContext = applicationContext;
        }
    }

    @Data
    private static class PaypalPurchaseUnit {
        private PaypalMoney amount;
        private PaypalPayments payments; // Used in response
        @JsonProperty("reference_id")
        private String referenceId;
        public PaypalPurchaseUnit(PaypalMoney amount) { this.amount = amount; }
        public PaypalPurchaseUnit() {}
    }

    @Data
    private static class PaypalMoney {
        @JsonProperty("currency_code")
        private String currencyCode;
        private String value;
        public PaypalMoney(String currencyCode, String value) { this.currencyCode = currencyCode; this.value = value; }
        public PaypalMoney() {}
    }

    @Data
    private static class PaypalApplicationContext {
        @JsonProperty("return_url")
        private String returnUrl;
        @JsonProperty("cancel_url")
        private String cancelUrl;
        public PaypalApplicationContext(String returnUrl, String cancelUrl) { this.returnUrl = returnUrl; this.cancelUrl = cancelUrl; }
        public PaypalApplicationContext() {}
    }

    @Data
    private static class PaypalOrderResponse {
        private String id;
        private String status;
        private List<PaypalLink> links;
        @JsonProperty("purchase_units")
        private List<PaypalPurchaseUnit> purchaseUnits;
    }

    @Data
    private static class PaypalLink {
        private String href;
        private String rel;
        private String method;
    }

    @Data
    private static class PaypalPayments {
        private List<PaypalCapture> captures;
    }

    @Data
    private static class PaypalCapture {
        private String id;
        private String status;
    }

    @Data
    private static class PaypalRefundRequest {
        private PaypalMoney amount;
        public PaypalRefundRequest(PaypalMoney amount) { this.amount = amount; }
        public PaypalRefundRequest() {}
    }

    @Data
    private static class PaypalRefundResponse {
        private String id;
        private String status;
    }
}
