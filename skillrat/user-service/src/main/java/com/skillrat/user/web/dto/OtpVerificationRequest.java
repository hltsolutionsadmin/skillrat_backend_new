package com.skillrat.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OtpVerificationRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,
    
    @NotBlank(message = "OTP is required")
    String otp
) {}
