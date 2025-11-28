package com.skillrat.project.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WBSAllocationDTO {
    private UUID id;
    private UUID memberId;
    private UUID wbsElementId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private String createdBy;
    private String updatedBy;
}
