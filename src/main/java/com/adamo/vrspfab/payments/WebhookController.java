package com.adamo.vrspfab.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "webhooks.enabled", havingValue = "true")
public class WebhookController {

    private final WebhookService webhookService;

    @Value("${paypal.webhook.id}")
    private String paypalWebhookId;


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
