package com.skillrat.user.dto;

import com.skillrat.user.domain.SalaryComponent.ComponentType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayslipDTO {

    @NotNull
    private UUID employeeId;
    private int month;
    private int year;
    private UUID id;
    private BigDecimal totalEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal unpaidLeaveDeduction;
    private BigDecimal netSalary;
    private OffsetDateTime generatedAt;
    private List<ComponentDTO> components;
}

