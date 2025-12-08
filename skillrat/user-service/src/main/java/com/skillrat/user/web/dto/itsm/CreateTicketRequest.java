package com.skillrat.user.web.dto.itsm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTicketRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;
    
    @NotNull(message = "Category is required")
    private TicketCategory category;
    
    @NotNull(message = "Priority is required")
    private TicketPriority priority;
    
    @Size(max = 50, message = "Asset ID must be less than 50 characters")
    private String assetId;
    
    @Size(max = 100, message = "Location must be less than 100 characters")
    private String location;
    
    public enum TicketCategory {
        HARDWARE,
        SOFTWARE,
        NETWORK,
        EMAIL,
        ACCOUNT,
        OTHER
    }
    
    public enum TicketPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
