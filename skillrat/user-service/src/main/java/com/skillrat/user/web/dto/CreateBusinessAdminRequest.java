package com.skillrat.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class CreateBusinessAdminRequest {
    @NotBlank public String firstName;
    @NotBlank public String lastName;
    @NotBlank @Email public String email;
    public String mobile;
    public UUID b2bUnitId;
}
