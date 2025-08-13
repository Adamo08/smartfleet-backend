package com.adamo.vrspfab.notifications;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendNotificationEmail(String to, String subject, String templateName, Map<String, Object> templateModel) {
        Context context = new Context();
        context.setVariables(templateModel);

        String htmlBody = templateEngine.process(templateName, context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            // No need to set From here, it's done in config (app yaml file)
            // helper.setFrom("noreply@vrspfab.com");

            mailSender.send(mimeMessage);
            log.info("Sent email notification to {} with subject '{}'", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Optionally, throw a custom exception or queue for retry
        }
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