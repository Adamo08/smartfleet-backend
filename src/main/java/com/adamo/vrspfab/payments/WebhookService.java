package com.adamo.vrspfab.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaypalPaymentProvider paypalPaymentProvider;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${paypal.api.baseUrl}")
    private String paypalBaseUrl;

    @Transactional
    public void processPaypalEvent(String payload, HttpHeaders headers, String webhookId) {
        // Extract required headers
        String transmissionId = headers.getFirst("PAYPAL-TRANSMISSION-ID");
        String transmissionTime = headers.getFirst("PAYPAL-TRANSMISSION-TIME");
        String transmissionSig = headers.getFirst("PAYPAL-TRANSMISSION-SIG");
        String certUrl = headers.getFirst("PAYPAL-CERT-URL");
        String authAlgo = headers.getFirst("PAYPAL-AUTH-ALGO");

        if (transmissionId == null || transmissionTime == null || transmissionSig == null || certUrl == null || authAlgo == null) {
            log.error("Missing PayPal webhook headers.");
            throw new PaymentException("Invalid webhook headers.");
        }

        try {
            // Parse payload to Map for verification and processing
            @SuppressWarnings("unchecked")
            Map<String, Object> webhookEvent = objectMapper.readValue(payload, Map.class);

            // Build verification request
            Map<String, Object> verificationRequest = Map.of(
                    "auth_algo", authAlgo,
                    "cert_url", certUrl,
                    "transmission_id", transmissionId,
                    "transmission_sig", transmissionSig,
                    "transmission_time", transmissionTime,
                    "webhook_id", webhookId,
                    "webhook_event", webhookEvent
            );

            HttpHeaders verifyHeaders = new HttpHeaders();
            verifyHeaders.setBearerAuth(paypalPaymentProvider.getAccessToken());
            verifyHeaders.setContentType(MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Object>> verifyEntity = new org.springframework.http.HttpEntity<>(verificationRequest, verifyHeaders);

            ResponseEntity<Map> verifyResponse = restTemplate.exchange(
                    paypalBaseUrl + "/v1/notifications/verify-webhook-signature",
                    HttpMethod.POST,
                    verifyEntity,
                    Map.class
            );

            @SuppressWarnings("unchecked")
            String status = (String) verifyResponse.getBody().get("verification_status");
            if (!"SUCCESS".equals(status)) {
                log.error("PayPal webhook verification failed: {}", status);
                throw new PaymentException("Webhook verification failed.");
            }

            // Verification passed; process event
            String eventType = (String) webhookEvent.get("event_type");
            log.info("Processing verified PayPal event: {} ({})", webhookEvent.get("id"), eventType);

            if ("PAYMENT.CAPTURE.COMPLETED".equals(eventType)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resource = (Map<String, Object>) webhookEvent.get("resource");
                String transactionId = (String) resource.get("id");
                String providerStatus = (String) resource.get("status");
                handlePaymentIntent(transactionId, providerStatus);
            } else if ("PAYMENT.CAPTURE.REFUNDED".equals(eventType)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resource = (Map<String, Object>) webhookEvent.get("resource");
                String refundId = (String) resource.get("id");
                String providerStatus = (String) resource.get("status");
                handleRefundUpdate(refundId, providerStatus);
            }
            // We will add handling for other event types as needed, e.g., PAYMENT.CAPTURE.DENIED, PAYMENT.CAPTURE.REVERSED

        } catch (Exception e) {
            log.error("Error processing PayPal webhook: {}", e.getMessage(), e);
            throw new PaymentException("Failed to process PayPal webhook.", e);
        }
    }

    private void handlePaymentIntent(String transactionId, String providerStatus) {
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionId(transactionId);
        if (paymentOpt.isEmpty()) {
            log.warn("Received webhook for unknown transaction ID: {}", transactionId);
            return;
        }

        Payment payment = paymentOpt.get();
        PaymentStatus newStatus = mapProviderStatus(providerStatus);

        if (payment.getStatus() != newStatus) {
            log.info("Updating payment {} from {} to {}", payment.getId(), payment.getStatus(), newStatus);
            payment.setStatus(newStatus);
            paymentRepository.save(payment);
            // Here we could trigger other business logic, like sending a confirmation email.
        } else {
            log.info("Payment {} already has status {}. No update needed.", payment.getId(), newStatus);
        }
    }

    private void handleRefundUpdate(String refundId, String providerStatus) {
        Optional<Refund> refundOpt = refundRepository.findByRefundTransactionId(refundId);
        if (refundOpt.isEmpty()) {
            log.warn("Received webhook for unknown refund ID: {}", refundId);
            return;
        }

        Refund refund = refundOpt.get();
        RefundStatus newStatus = "COMPLETED".equalsIgnoreCase(providerStatus) ? RefundStatus.PROCESSED : RefundStatus.FAILED;

        if (refund.getStatus() != newStatus) {
            log.info("Updating refund {} to status {}", refund.getId(), newStatus);
            refund.setStatus(newStatus);
            refundRepository.save(refund);

            Payment payment = refund.getPayment();
            if (payment != null && newStatus == RefundStatus.PROCESSED) {
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            }
        } else {
            log.info("Refund {} already has status {}. No update needed.", refund.getId(), newStatus);
        }
    }

    private PaymentStatus mapProviderStatus(String status) {
        if (status == null) return PaymentStatus.PENDING;
        return switch (status.toLowerCase()) {
            case "succeeded", "completed", "approved" -> PaymentStatus.COMPLETED;
            case "failed", "requires_payment_method" -> PaymentStatus.FAILED;
            case "canceled" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }
}