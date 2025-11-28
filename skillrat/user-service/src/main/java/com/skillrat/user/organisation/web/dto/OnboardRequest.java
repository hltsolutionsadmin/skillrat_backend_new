package com.skillrat.user.organisation.web.dto;

import com.skillrat.user.organisation.domain.B2BUnitType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnboardRequest {
    @NotBlank private String name;
    @NotNull private B2BUnitType type;
    @Email private String contactEmail;
    private String contactPhone;
    private String website;
    private AddressDTO address;
    private String groupName; // optional

    // Admin-specific (optional for self onboard)
    private String approver;
    private String adminFirstName;
    private String adminLastName;
    @Email private String adminEmail;
    private String adminMobile;
}
