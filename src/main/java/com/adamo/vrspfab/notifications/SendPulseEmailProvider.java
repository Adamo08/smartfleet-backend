package com.adamo.vrspfab.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "sendpulse")
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

    @PostConstruct
    public void logAvailability() {
        boolean hasId = apiId != null && !apiId.isEmpty();
        boolean hasSecret = apiSecret != null && !apiSecret.isEmpty();
        String maskedId = hasId ? apiId.substring(0, Math.min(4, apiId.length())) + "***" : "<empty>";
        log.info("[SendPulse] Provider ready. API ID set: {}, API Secret set: {}, From: {} <{}>. API ID preview: {}",
                hasId, hasSecret, fromName, fromEmail, maskedId);

        // Perform preflight validation against SendPulse API
        try {
            String accessToken = obtainAccessToken();
            if (accessToken == null) {
                log.warn("[SendPulse] Skipping preflight checks: failed to obtain access token.");
                return;
            }

            boolean senderAllowed = false;
            boolean domainAllowed = false;

            // Check allowed SMTP senders
            try {
                ResponseEntity<String> sendersResp = getWithBearer("https://api.sendpulse.com/smtp/senders", accessToken);
                if (sendersResp.getStatusCode().is2xxSuccessful() && sendersResp.getBody() != null) {
                    senderAllowed = bodyContainsCaseInsensitiveEmail(sendersResp.getBody(), fromEmail);
                    log.info("[SendPulse] SMTP senders fetched. Configured from '{}' {} in allowed senders.",
                            fromEmail, senderAllowed ? "found" : "NOT found");
                } else {
                    log.warn("[SendPulse] Failed to fetch SMTP senders. Status: {}, Body: {}",
                            sendersResp.getStatusCode(), sendersResp.getBody());
                }
            } catch (Exception e) {
                log.warn("[SendPulse] Error fetching SMTP senders: {}", e.getMessage());
            }

            // Check allowed/verified domains
            try {
                ResponseEntity<String> domainsResp = getWithBearer("https://api.sendpulse.com/smtp/domains", accessToken);
                if (domainsResp.getStatusCode().is2xxSuccessful() && domainsResp.getBody() != null) {
                    String domain = extractDomain(fromEmail);
                    domainAllowed = bodyContainsCaseInsensitive(domainsResp.getBody(), domain);
                    log.info("[SendPulse] SMTP domains fetched. Domain '{}' {} in allowed domains.",
                            domain, domainAllowed ? "found" : "NOT found");
                } else {
                    log.warn("[SendPulse] Failed to fetch SMTP domains. Status: {}, Body: {}",
                            domainsResp.getStatusCode(), domainsResp.getBody());
                }
            } catch (Exception e) {
                log.warn("[SendPulse] Error fetching SMTP domains: {}", e.getMessage());
            }

            if (!senderAllowed && !domainAllowed) {
                log.warn("[SendPulse] ⚠️ Configured From '{}' is not present in SMTP senders and its domain is not in allowed domains. Emails may be rejected with 'Sender is not valid'.", fromEmail);
            } else if (!senderAllowed) {
                log.warn("[SendPulse] ⚠️ Configured From '{}' is not present in SMTP senders. Consider adding/activating it, or ensure domain-based sending is enabled.", fromEmail);
            } else if (!domainAllowed) {
                log.warn("[SendPulse] ⚠️ Domain of From '{}' is not in allowed/verified domains. Consider verifying the domain.", fromEmail);
            } else {
                log.info("[SendPulse] ✅ Preflight OK: From '{}' appears permitted (sender or domain).", fromEmail);
            }
        } catch (Exception e) {
            log.warn("[SendPulse] Preflight checks failed: {}", e.getMessage());
        }
    }

    @Override
    public boolean sendEmail(String to, String subject, String htmlContent) {
        try {
            log.info("Sending email via SendPulse to: {}", to);

            // Obtain OAuth access token
            String accessToken = obtainAccessToken();
            if (accessToken == null) {
                log.error("❌ SendPulse auth failed. No access token.");
                return false;
            }

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
                String[] parsed = parseSendPulseError(response.getBody());
                log.error("❌ Failed to send email via SendPulse. Status: {}, ErrorCode: {}, Message: {}, Raw: {}",
                        response.getStatusCode(), parsed[0], parsed[1], response.getBody());
                return false;
            }

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            String[] parsed = parseSendPulseError(responseBody);
            log.error("❌ Error sending email via SendPulse to {}: HTTP {}. ErrorCode: {}, Message: {}, Raw: {}",
                    to, e.getStatusCode(), parsed[0], parsed[1], responseBody, e);
            return false;
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

    private String obtainAccessToken() {
        try {
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
                return null;
            }
            return String.valueOf(tokenResponse.getBody().get("access_token"));
        } catch (Exception e) {
            log.error("❌ SendPulse auth request failed: {}", e.getMessage());
            return null;
        }
    }

    private ResponseEntity<String> getWithBearer(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    private boolean bodyContainsCaseInsensitiveEmail(String body, String email) {
        if (body == null || email == null) {
            return false;
        }
        return body.toLowerCase().contains(email.toLowerCase());
    }

    private boolean bodyContainsCaseInsensitive(String body, String value) {
        if (body == null || value == null) {
            return false;
        }
        return body.toLowerCase().contains(value.toLowerCase());
    }

    private String extractDomain(String email) {
        if (email == null) {
            return "";
        }
        int at = email.indexOf('@');
        return at >= 0 ? email.substring(at + 1) : "";
    }

    private String[] parseSendPulseError(String body) {
        String code = "";
        String message = "";
        if (body == null || body.isEmpty()) {
            return new String[]{code, message};
        }
        try {
            Map<?, ?> map = objectMapper.readValue(body, Map.class);
            Object msg = map.get("message");
            Object err = map.get("error_code");
            message = msg != null ? String.valueOf(msg) : message;
            code = err != null ? String.valueOf(err) : code;
        } catch (Exception ignored) {
            // leave defaults
        }
        return new String[]{code, message};
    }
}
