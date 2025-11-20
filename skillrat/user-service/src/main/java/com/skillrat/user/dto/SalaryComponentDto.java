package com.skillrat.user.dto;

import com.skillrat.user.domain.SalaryComponent.ComponentType;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryComponentDto {
    private UUID id;
    private String name;
    private String code;
    private ComponentType type;
    private String description;
    private BigDecimal amount; // optional for structure responses
}
