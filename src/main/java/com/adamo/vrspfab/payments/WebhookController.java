package com.adamo.vrspfab.payments;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @Value("${paypal.webhook.id}")
    private String paypalWebhookId;


    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        if (sigHeader == null) {
            return ResponseEntity.badRequest().body("Missing Stripe-Signature header.");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook signature verification failed.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature verification failed.");
        } catch (Exception e) {
            log.error("Error processing Stripe webhook.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook.");
        }

        webhookService.processStripeEvent(event);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/paypal")
    public ResponseEntity<String> handlePaypalWebhook(@RequestBody String payload, @RequestHeader HttpHeaders headers) {
        // PayPal webhook validation is more complex and requires using their SDK or manual signature verification
        // For brevity, we'll delegate the verification and processing to the service layer
        try {
            webhookService.processPaypalEvent(payload, headers, paypalWebhookId);
        } catch (Exception e) {
            log.error("Error processing PayPal webhook.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook.");
        }

        return ResponseEntity.ok().build();
    }
}
