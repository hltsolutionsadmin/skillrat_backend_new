package com.skillrat.user.web.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank public String emailOrMobile;
    @NotBlank public String password;
}
