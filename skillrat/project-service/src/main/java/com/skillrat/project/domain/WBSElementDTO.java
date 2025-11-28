package com.skillrat.project.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WBSElementDTO {
    private UUID id;
    private UUID projectId;
    private String name;
    private String code;
    private WBSCategory category;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean disabled;
}
