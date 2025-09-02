package com.adamo.vrspfab.notifications;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from:}")
    private String fromEmail;

    @Override
    public boolean sendEmail(String to, String subject, String htmlContent) {
        try {
            log.info("Sending email via SMTP to: {}", to);
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            if (fromEmail != null && !fromEmail.isEmpty()) {
                helper.setFrom(fromEmail);
            }
            
            mailSender.send(mimeMessage);
            log.info("✅ Email sent successfully via SMTP to: {}", to);
            return true;
            
        } catch (MessagingException e) {
            log.error("❌ Failed to send email via SMTP to {}: {}", to, e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "SMTP";
    }

    @Override
    public boolean isAvailable() {
        return mailSender != null;
    }
}
