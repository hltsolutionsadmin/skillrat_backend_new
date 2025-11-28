package com.skillrat.project.web.request;

import com.skillrat.project.domain.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpsertMemberRequest {
    @NotNull public UUID employeeId;
    public ProjectRole role;
    public UUID reportingManagerId;
    public LocalDate startDate;
    public LocalDate endDate;
    public Boolean active;
}
