package com.skillrat.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HolidayRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    @NotNull(message = "Year is required")
    private Integer year;
    
    private boolean recurring = false;
    
    private boolean active = true;
}
