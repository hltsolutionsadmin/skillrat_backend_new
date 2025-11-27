package com.skillrat.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SignupRequest {
    @NotBlank public String firstName;
    @NotBlank public String lastName;
    @NotBlank @Email public String email;
    public String mobile;
    @NotBlank public String password;
}
