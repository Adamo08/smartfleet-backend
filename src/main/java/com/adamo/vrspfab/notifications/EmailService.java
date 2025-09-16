package com.adamo.vrspfab.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final List<EmailProvider> emailProviders;
    private final TemplateEngine templateEngine;

    @Value("${email.provider.priority:sendpulse,smtp}")
    private String providerPriority;

    @PostConstruct
    public void logProviderAvailability() {
        log.info("[Email] Provider priority: {}", providerPriority);
        for (EmailProvider provider : emailProviders) {
            boolean available = false;
            try {
                available = provider.isAvailable();
            } catch (Exception e) {
                log.warn("[Email] Provider {} availability check threw: {}", provider.getProviderName(), e.getMessage());
            }
            log.info("[Email] Provider {} available: {}", provider.getProviderName(), available);
        }
    }

    @Async
    public void sendNotificationEmail(String to, String subject, String templateName, Map<String, Object> templateModel) {
        Context context = new Context();
        context.setVariables(templateModel);

        String htmlBody = templateEngine.process(templateName, context);
        
        // Try to send email using available providers in priority order
        boolean emailSent = sendEmailWithFallback(to, subject, htmlBody);
        
        if (emailSent) {
            log.info("✅ Email notification sent successfully to {} with subject '{}'", to, subject);
        } else {
            log.error("❌ Failed to send email to {} using all available providers", to);
            // Optionally, throw a custom exception or queue for retry
        }
    }

    /**
     * Sends email using available providers in priority order with fallback.
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML content
     * @return true if email was sent successfully, false otherwise
     */
    private boolean sendEmailWithFallback(String to, String subject, String htmlContent) {
        String[] priorities = providerPriority.split(",");
        
        for (String priority : priorities) {
            String providerName = priority.trim().toLowerCase();
            
            for (EmailProvider provider : emailProviders) {
                if (provider.getProviderName().toLowerCase().equals(providerName) && provider.isAvailable()) {
                    log.info("Attempting to send email using {} provider", provider.getProviderName());
                    
                    if (provider.sendEmail(to, subject, htmlContent)) {
                        log.info("✅ Email sent successfully using {} provider", provider.getProviderName());
                        return true;
                    } else {
                        log.warn("⚠️ Failed to send email using {} provider, trying next provider", provider.getProviderName());
                    }
                }
            }
        }
        
        log.error("❌ All email providers failed to send email to {}", to);
        return false;
    }



    /**
     * Sends a welcome email to a new user.
     *
     * @param to The recipient's email address.
     * @param username The username of the new user.
     */
    @Async
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to VRSPFAB!";
        String templateName = "welcome-email";
        Map<String, Object> templateModel = Map.of("username", username);
        log.info("Sending {} email notification to {}", subject, to);
        sendNotificationEmail(to, subject, templateName, templateModel);
    }


    /**
     * Sends a password reset email with a reset link.
     *
     * @param to The recipient's email address.
     * @param resetLink The link to reset the password.
     */
    @Async
    public void sendResetPasswordEmail(String to, String resetLink) {
        String subject = "Reset Your Password";
        String templateName = "reset-password-email";
        Map<String, Object> templateModel = Map.of("resetLink", resetLink);

        log.info("Sending {} email notification to {}", subject, resetLink);

        sendNotificationEmail(to, subject, templateName, templateModel);
    }
}