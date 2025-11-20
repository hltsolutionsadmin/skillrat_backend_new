package com.skillrat.user.service;

import com.skillrat.user.dto.SalaryStructureDtos;

import java.util.Optional;
import java.util.UUID;

public interface SalaryStructureService {
    SalaryStructureDtos.StructureResponse upsert(SalaryStructureDtos.UpsertRequest req);
    Optional<SalaryStructureDtos.StructureResponse> getLatestByEmployee(UUID employeeId);
}
