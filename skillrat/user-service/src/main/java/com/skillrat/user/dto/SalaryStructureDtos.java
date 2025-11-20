package com.skillrat.user.dto;

import com.skillrat.user.domain.SalaryComponent.ComponentType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class SalaryStructureDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ComponentAmount {
        @NotNull
        private UUID componentId;
        @NotNull
        private BigDecimal amount;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpsertRequest {
        @NotNull
        private UUID employeeId;
        @NotNull
        private BigDecimal ctc;
        @NotNull
        private BigDecimal grossSalary;
        @NotNull
        private LocalDate effectiveFrom;
        @NotNull
        private List<ComponentAmount> components;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StructureComponentResp {
        private UUID componentId;
        private String code;
        private String name;
        private ComponentType type;
        private BigDecimal amount;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StructureResponse {
        private UUID id;
        private UUID employeeId;
        private BigDecimal ctc;
        private BigDecimal grossSalary;
        private LocalDate effectiveFrom;
        private List<StructureComponentResp> components;
    }
}
