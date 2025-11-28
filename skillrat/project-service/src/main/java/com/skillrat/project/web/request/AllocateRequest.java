package com.skillrat.project.web.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AllocateRequest {
    @NotNull public UUID wbsId;
    public LocalDate startDate;
    public LocalDate endDate;
}
