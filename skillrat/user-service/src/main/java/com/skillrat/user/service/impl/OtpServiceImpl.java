package com.skillrat.user.service.impl;

import com.skillrat.user.service.EmailService;
import com.skillrat.user.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OtpServiceImpl implements OtpService {
    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);
    private final EmailService emailService;

    public OtpServiceImpl(EmailService emailService) {
        this.emailService = emailService;
    }
    
    // In-memory storage for OTPs with expiration (in a production environment, consider using Redis)
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private static final long OTP_VALID_DURATION = 5; // 5 minutes
    private final Random random = new Random();

    @Override
    public void sendOtp(String email) {
        try {
            // Generate a 6-digit OTP
            String otp = String.format("%06d", random.nextInt(1000000));
            
            // Store OTP with timestamp
            otpStorage.put(email, new OtpData(otp, System.currentTimeMillis()));
            
            // Send OTP via email
            emailService.sendOtpEmail(email, otp);
            
            log.info("OTP generated and sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP. Please try again later.", e);
        }
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        try {
            log.debug("Verifying OTP for email: {}", email);
            
            OtpData otpData = otpStorage.get(email);
            if (otpData == null) {
                log.warn("No OTP found for email: {}", email);
                return false; // No OTP found for this email
            }
            
            // Check if OTP has expired (5 minutes)
            long currentTime = System.currentTimeMillis();
            long otpAgeInMinutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - otpData.getTimestamp());
            
            if (otpAgeInMinutes > OTP_VALID_DURATION) {
                log.warn("OTP expired for email: {}, age: {} minutes", email, otpAgeInMinutes);
                otpStorage.remove(email); // Clean up expired OTP
                return false;
            }
            
            // Verify the OTP
            boolean isValid = otpData.getOtp().equals(otp);
            log.debug("OTP verification for email: {} - valid: {}", email, isValid);
            
            if (isValid) {
                otpStorage.remove(email); // Remove used OTP
                log.info("OTP verified successfully for email: {}", email);
            } else {
                log.warn("Invalid OTP provided for email: {}", email);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying OTP for email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error verifying OTP. Please try again.", e);
        }
    }
    
    // Inner class to hold OTP data and timestamp
    private static class OtpData {
        private final String otp;
        private final long timestamp;
        
        public OtpData(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }
        
        public String getOtp() {
            return otp;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}
