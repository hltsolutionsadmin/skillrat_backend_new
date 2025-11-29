package com.skillrat.user.web.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class EmployeeBandRequestDTO {
    private String name;
    private Integer experienceMin;
    private Integer experienceMax;
    private Double salary;
    private UUID b2bUnitId;
}
