package com.skillrat.user.dto;

import java.util.UUID;

public class RoleDto {
    private UUID id;
    private String name;
    private UUID b2bUnitId;

    public RoleDto() {}

    public RoleDto(UUID id, String name, UUID b2bUnitId) {
        this.id = id;
        this.name = name;
        this.b2bUnitId = b2bUnitId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getB2bUnitId() { return b2bUnitId; }
    public void setB2bUnitId(UUID b2bUnitId) { this.b2bUnitId = b2bUnitId; }
}
