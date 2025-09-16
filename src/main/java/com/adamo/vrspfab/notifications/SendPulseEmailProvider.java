package com.adamo.vrspfab.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SendPulseEmailProvider implements EmailProvider {

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

    public SendPulseEmailProvider(@Qualifier("emailRestTemplate") RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean sendEmail(String to, String subject, String htmlContent) {
        try {
            log.info("Sending email via SendPulse to: {}", to);

            // Obtain OAuth access token
            String tokenUrl = "https://api.sendpulse.com/oauth/access_token";
            Map<String, Object> tokenRequest = new HashMap<>();
            tokenRequest.put("grant_type", "client_credentials");
            tokenRequest.put("client_id", apiId);
            tokenRequest.put("client_secret", apiSecret);

            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> tokenEntity = new HttpEntity<>(tokenRequest, tokenHeaders);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenEntity, Map.class);
            if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null || tokenResponse.getBody().get("access_token") == null) {
                log.error("❌ SendPulse auth failed. Status: {}, Body: {}", tokenResponse.getStatusCode(), tokenResponse.getBody());
                return false;
            }
            String accessToken = String.valueOf(tokenResponse.getBody().get("access_token"));

            // Send email using Bearer token
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

            // Raw HTML body for SendPulse smtp/emails endpoint
            emailData.put("html", htmlContent);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", emailData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Email sent successfully via SendPulse to: {}", to);
                return true;
            } else {
                log.error("❌ Failed to send email via SendPulse. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
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
