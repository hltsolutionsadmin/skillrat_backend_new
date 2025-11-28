package com.skillrat.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.skillrat.project.domain.ProjectStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Set;

/**
 * Response DTO for Project data.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponse extends BaseResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectClientDTO client;
    private Set<String> tags;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
