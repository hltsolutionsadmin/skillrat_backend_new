package com.skillrat.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String frontendBaseUrl;
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    public MailService(JavaMailSender mailSender,
                       @Value("${spring.mail.username}") String fromAddress,
                       @Value("${skillrat.frontend.base-url:http://localhost:4200}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Async("taskExecutor")
    public void sendPasswordSetupEmail(String toEmail, String displayName, String token) {
        try {
            if (toEmail == null || token == null) {
                log.warn("Email or token is null. Email: {}, Token present: {}", toEmail, token != null);
                return;
            }
            
            String subject = "Set up your SkillRat account password";
            String greeting = (displayName != null && !displayName.isBlank()) ? ("Hi " + displayName + ",") : "Hi,";
            String resetLink = String.format("%s/set-password?token=%s", frontendBaseUrl, token);
            String text = String.format(
                "%s%n%n" +
                "An account has been created for you. Please set your password by clicking the link below:%n%n" +
                "%s%n%n" +
                "This link will expire in 7 days.%n%n" +
                "If you didn't expect this email, you can safely ignore it.",
                greeting, resetLink
            );

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toEmail);
            msg.setSubject(subject);
            msg.setText(text);
            
            log.info("Sending password setup email to {}", toEmail);
            mailSender.send(msg);
            log.info("Password setup email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password setup email to {}: {}", toEmail, e.getMessage(), e);
            // In a production environment, you might want to retry or notify an admin
        }
    }
}
