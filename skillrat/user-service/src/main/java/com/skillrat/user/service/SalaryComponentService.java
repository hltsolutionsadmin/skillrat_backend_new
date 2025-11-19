package com.skillrat.user.service;

import com.skillrat.user.dto.SalaryComponentDto;

import java.util.List;
import java.util.UUID;

public interface SalaryComponentService {
    SalaryComponentDto create(SalaryComponentDto dto);
    List<SalaryComponentDto> list();
    SalaryComponentDto update(UUID id, SalaryComponentDto dto);
    void delete(UUID id);
}
