package com.skillrat.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private final int otpLength;
    private final long otpExpiryMs;
    private final MailService mailService;
    private final UserService userService;
    private final OAuth2TokenService tokenService;
    private final Random random = new Random();

    @Autowired
    public OtpService(
            MailService mailService,
            UserService userService,
            OAuth2TokenService tokenService,
            @Value("${app.otp.length:6}") int otpLength,
            @Value("${app.otp.expiry-minutes:10}") int otpExpiryMinutes) {
        this.mailService = mailService;
        this.userService = userService;
        this.tokenService = tokenService;
        this.otpLength = otpLength;
        this.otpExpiryMs = otpExpiryMinutes * 60 * 1000L;
    }

    public boolean sendOtp(String email) {
        return userService.findByEmail(email).map(user -> {
            String otp = generateOtp();
            long expiryTime = System.currentTimeMillis() + otpExpiryMs;
            otpStore.put(email.toLowerCase(), new OtpData(otp, expiryTime));
            mailService.sendOtpEmail(email, otp);
            log.info("OTP generated for email: {}", email);
            return true;
        }).orElse(false);
    }

    public Optional<String> verifyOtpAndGetToken(String email, String otp) {
        try {
            OtpData otpData = otpStore.get(email.toLowerCase());
            if (otpData == null) {
                log.warn("No OTP found for email: {}", email);
                return Optional.empty();
            }

            if (System.currentTimeMillis() > otpData.expiryTime) {
                log.warn("OTP expired for email: {}", email);
                otpStore.remove(email.toLowerCase());
                return Optional.empty();
            }

            boolean isValid = otpData.otp.equals(otp);
            if (isValid) {
                otpStore.remove(email.toLowerCase());
                log.info("OTP verified successfully for email: {}", email);
                
                // Get the user to verify they exist and get their actual password
                return userService.findByEmail(email)
                    .map(user -> {
                        try {
                            // Use the user's actual password from the database
                            String token = tokenService.getTokenForUser(email, user.getPasswordHash());
                            if (token == null) {
                                log.error("Failed to get token for user: {}", email);
                            }
                            return token;
                        } catch (Exception e) {
                            log.error("Error getting token for user: " + email, e);
                            return null;
                        }
                    });
            } else {
                log.warn("Invalid OTP provided for email: {}", email);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error in verifyOtpAndGetToken for email: " + email, e);
            return Optional.empty();
        }
    }

    @Scheduled(fixedRate = 3600000) // Clean up expired OTPs every hour
    public void cleanupExpiredOtps() {
        long now = System.currentTimeMillis();
        otpStore.entrySet().removeIf(entry -> entry.getValue().expiryTime < now);
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private static class OtpData {
        private final String otp;
        private final long expiryTime;
        private final String password; // Store temporary password for passwordless flow

        OtpData(String otp, long expiryTime) {
            this(otp, expiryTime, null);
        }

        OtpData(String otp, long expiryTime, String password) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.password = password;
        }
    }
}
