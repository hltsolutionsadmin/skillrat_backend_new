package com.skillrat.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class AssignBusinessAdminRequest {
    @NotBlank @Email public String email;
    public UUID b2bUnitId;
}
