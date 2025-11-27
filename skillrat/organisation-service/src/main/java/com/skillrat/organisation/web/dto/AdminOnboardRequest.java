package com.skillrat.organisation.web.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOnboardRequest extends SelfOnboardRequest {
    private String approver;
    private String adminFirstName;
    private String adminLastName;
    @Email private String adminEmail;
    private String adminMobile;
}
