package com.skillrat.expense.web.dto;

import com.skillrat.expense.domain.ExpenseCategory;
import com.skillrat.expense.domain.ExpenseStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ExpenseDTO {
    private UUID id;
    private BigDecimal amount;
    private String currency;
    private ExpenseCategory category;
    private ExpenseStatus status;
    private String description;
    private LocalDate incurredDate;
    private Instant createdDate;
    private Instant updatedDate;
}
