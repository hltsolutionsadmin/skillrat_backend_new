package com.skillrat.user.organisation.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DepartmentDTO {
    private UUID id;
    private String name;
    private String description;
    private boolean active;
    private String code;
    private UUID b2bUnitId;
}
