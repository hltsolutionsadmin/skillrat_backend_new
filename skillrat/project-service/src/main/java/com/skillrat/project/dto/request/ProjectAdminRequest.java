package com.skillrat.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ProjectAdminRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Code is required")
    private String code;
    
    private String description;
    
    @NotNull(message = "B2B Unit ID is required")
    private UUID b2bUnitId;
    
    private String clientName;
    private String clientPrimaryEmail;
    private String clientSecondaryEmail;
    
    private boolean active = true;
}
