package com.skillrat.user.dto;


import com.skillrat.user.domain.SalaryComponent;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComponentDTO {
    private String name;
    private SalaryComponent.ComponentType type;
    private BigDecimal amount;
}
