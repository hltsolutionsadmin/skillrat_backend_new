package com.skillrat.user.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DesignationDTO {

    private UUID designationId;
    private String designationName;
    private String bandName;
    private Long resourceCount;

    // Getters and setters
    public UUID getDesignationId() { return designationId; }
    public void setDesignationId(UUID designationId) { this.designationId = designationId; }
    public String getDesignationName() { return designationName; }
    public void setDesignationName(String designationName) { this.designationName = designationName; }
    public String getBandName() { return bandName; }
    public void setBandName(String bandName) { this.bandName = bandName; }
    public Long getResourceCount() { return resourceCount; }
    public void setResourceCount(Long resourceCount) { this.resourceCount = resourceCount; }
}
