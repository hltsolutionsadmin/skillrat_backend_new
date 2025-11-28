package com.skillrat.project.web.request;

import com.skillrat.project.domain.WBSCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateWbsRequest {
    @NotBlank public String name;
    public String code;
    public WBSCategory category;
    public LocalDate startDate;
    public LocalDate endDate;
}
