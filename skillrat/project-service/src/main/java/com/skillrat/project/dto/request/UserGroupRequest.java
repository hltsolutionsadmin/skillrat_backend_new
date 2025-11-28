package com.skillrat.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UserGroupRequest {
    @NotBlank(message = "Group name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Group type is required")
    private String groupType;
    
    private Set<UUID> memberIds;
    
    private UUID managerId;
    
    private boolean active = true;
}
