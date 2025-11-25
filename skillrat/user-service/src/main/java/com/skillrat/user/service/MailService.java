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

    @Async
    public void sendPasswordSetupEmail(String toEmail, String displayName, String token) {
        if (toEmail == null || token == null) return;
        String subject = "Set up your SkillRat account password";
        String greeting = (displayName != null && !displayName.isBlank()) ? ("Hi " + displayName + ",") : "Hi,";
        String text = greeting + "\n\n" +
                "An account has been created for you. Please set your password in the application." + "\n\n" +
                "This link will expire in 7 days." + "\n\n" +
                "If you didn't expect this email, you can ignore it.";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject(subject);
        msg.setText(text);
        log.info("Sending password setup email to {}", toEmail);
        mailSender.send(msg);
        log.info("Password setup email sent to {}", toEmail);
    }
    
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        if (toEmail == null || otp == null) return;
        String subject = "Your SkillRat Login OTP";
        String text = "Your OTP for SkillRat login is: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes.\n" +
                "If you didn't request this OTP, please ignore this email or contact support.";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject(subject);
        msg.setText(text);
        log.info("Sending OTP email to {}", toEmail);
        mailSender.send(msg);
        log.info("OTP email sent to {}", toEmail);
    }
}
