package com.adamo.vrspfab.notifications;

/**
 * Interface for email providers to ensure scalability and flexibility.
 * This allows us to easily switch between different email services.
 */
public interface EmailProvider {
    
    /**
     * Sends an email using the specific provider implementation.
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendEmail(String to, String subject, String htmlContent);
    
    /**
     * Gets the name of the email provider.
     * 
     * @return Provider name (e.g., "SMTP", "SendPulse", "SendGrid")
     */
    String getProviderName();
    
    /**
     * Checks if the provider is available and configured.
     * 
     * @return true if provider is ready to use, false otherwise
     */
    boolean isAvailable();
}
