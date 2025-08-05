package com.adamo.vrspfab.payments;


import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void processStripeEvent(Event event) {
        log.info("Processing Stripe event: {} ({})", event.getId(), event.getType());

        Optional<StripeObject> stripeObject = event.getDataObjectDeserializer().getObject();
        if (stripeObject.isEmpty()) {
            log.warn("Stripe event {} has no data object.", event.getId());
            return;
        }

        if (stripeObject.get() instanceof PaymentIntent) {
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject.get();
            handlePaymentIntent(paymentIntent.getId(), paymentIntent.getStatus());
        }
        // Add cases for other objects like 'charge', 'refund', 'dispute', etc.
    }

    @Transactional
    public void processPaypalEvent(String payload, HttpHeaders headers, String webhookId) {
        // Here you would use the PayPal SDK to verify the webhook signature and deserialize the event
        // Example using a hypothetical modern SDK:
        /*
        try {
            Event paypalEvent = Webhook.verifyAndDeserialize(headers, payload, webhookId);
            log.info("Processing PayPal event: {} ({})", paypalEvent.getId(), paypalEvent.getEventType());

            if ("CHECKOUT.ORDER.APPROVED".equals(paypalEvent.getEventType())) {
                String transactionId = paypalEvent.getResource().getPurchaseUnits().get(0).getPayments().getCaptures().get(0).getId();
                handlePaymentIntent(transactionId, "succeeded");
            }
        } catch (PayPalSecurityException e) {
            log.error("PayPal webhook security validation failed!", e);
        }
        */
        log.info("PayPal webhook received. Implement verification and processing logic.");
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
            // Here you could trigger other business logic, like sending a confirmation email.
        } else {
            log.info("Payment {} already has status {}. No update needed.", payment.getId(), newStatus);
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
