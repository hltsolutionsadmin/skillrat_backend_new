package com.skillrat.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String frontendBaseUrl;

    public MailService(JavaMailSender mailSender,
                       @Value("${spring.mail.username}") String fromAddress,
                       @Value("${skillrat.frontend.base-url:http://localhost:4200}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public void sendPasswordSetupEmail(String toEmail, String displayName, String token) {
        if (toEmail == null || token == null) return;
        String subject = "Set up your SkillRat account password";
        String link = frontendBaseUrl.replaceAll("/$", "") + "/password/setup?token=" + token;
        String greeting = (displayName != null && !displayName.isBlank()) ? ("Hi " + displayName + ",") : "Hi,";
        String text = greeting + "\n\n" +
                "An account has been created for you. Please set your password using the link below:" + "\n\n" +
                link + "\n\n" +
                "This link will expire in 7 days." + "\n\n" +
                "If you didn't expect this email, you can ignore it.";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }
}
