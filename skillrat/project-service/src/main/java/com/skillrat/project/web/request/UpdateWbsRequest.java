package com.skillrat.project.web.request;

import com.skillrat.project.domain.WBSCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateWbsRequest {
    private String name;
    private String code;
    private UUID projectId;
    private WBSCategory category;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean disabled;
}
