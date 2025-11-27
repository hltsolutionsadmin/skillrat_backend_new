package com.skillrat.user.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public class InviteEmployeeRequest {
    @NotBlank public String firstName;
    @NotBlank public String lastName;
    @NotBlank @Email public String email;
    public String mobile;
    @NotEmpty public List<UUID> roleIds;
}
