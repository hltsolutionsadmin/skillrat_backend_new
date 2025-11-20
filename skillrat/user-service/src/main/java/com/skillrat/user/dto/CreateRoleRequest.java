package com.skillrat.user.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class CreateRoleRequest {
    private UUID uid;
    private UUID b2bUnitId;
    @NotBlank private String name;

    public UUID getUid() { return uid; }
    public void setUid(UUID uid) { this.uid = uid; }
    public UUID getB2bUnitId() { return b2bUnitId; }
    public void setB2bUnitId(UUID b2bUnitId) { this.b2bUnitId = b2bUnitId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
