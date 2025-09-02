package com.adamo.vrspfab.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendPulseEmailProvider implements EmailProvider {

    @Qualifier("emailRestTemplate")
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${sendpulse.api.id:}")
    private String apiId;

    @Value("${sendpulse.api.secret:}")
    private String apiSecret;

    @Value("${sendpulse.from.email:noreply@smartfleet.com}")
    private String fromEmail;

    @Value("${sendpulse.from.name:SmartFleet}")
    private String fromName;

    @Override
    public boolean sendEmail(String to, String subject, String htmlContent) {
        try {
            log.info("Sending email via SendPulse to: {}", to);

            // Prepare the request
            String url = "https://api.sendpulse.com/smtp/emails";
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("subject", subject);
            
            Map<String, String> from = new HashMap<>();
            from.put("name", fromName);
            from.put("email", fromEmail);
            emailData.put("from", from);
            
            Map<String, String> toRecipient = new HashMap<>();
            toRecipient.put("email", to);
            emailData.put("to", new Object[]{toRecipient});
            
            // Encode HTML content to base64
            String encodedContent = Base64.getEncoder().encodeToString(htmlContent.getBytes());
            emailData.put("html", encodedContent);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", emailData);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create basic auth header
            String auth = apiId + ":" + apiSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Send the request
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("✅ Email sent successfully via SendPulse to: {}", to);
                return true;
            } else {
                log.error("❌ Failed to send email via SendPulse. Status: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("❌ Error sending email via SendPulse to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "SendPulse";
    }

    @Override
    public boolean isAvailable() {
        return apiId != null && !apiId.isEmpty() && 
               apiSecret != null && !apiSecret.isEmpty() &&
               restTemplate != null;
    }
}
