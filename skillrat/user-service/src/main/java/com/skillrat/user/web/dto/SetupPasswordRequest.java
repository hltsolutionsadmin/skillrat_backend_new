package com.skillrat.user.web.dto;

import jakarta.validation.constraints.NotBlank;

public class SetupPasswordRequest {
    @NotBlank public String token;
    @NotBlank public String newPassword;
}
