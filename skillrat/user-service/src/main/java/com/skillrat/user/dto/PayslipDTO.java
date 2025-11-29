package com.skillrat.user.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

