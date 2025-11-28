package com.skillrat.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CatalogRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private UUID parentId;
    
    private String metadata;
    
    private boolean active = true;
}
