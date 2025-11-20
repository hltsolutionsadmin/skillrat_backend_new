package com.skillrat.user.service.impl;

import com.skillrat.user.repo.EmployeeSalaryComponentDao;
import com.skillrat.user.repo.EmployeeSalaryStructureDao;
import com.skillrat.user.repo.SalaryComponentDao;
import com.skillrat.user.dto.SalaryStructureDtos;
import com.skillrat.user.domain.EmployeeSalaryComponent;
import com.skillrat.user.domain.EmployeeSalaryStructure;
import com.skillrat.user.domain.SalaryComponent;
import com.skillrat.user.service.SalaryStructureService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalaryStructureServiceImpl implements SalaryStructureService {

    private final EmployeeSalaryStructureDao structureDao;
    private final EmployeeSalaryComponentDao structureCompDao;
    private final SalaryComponentDao componentDao;

    public SalaryStructureServiceImpl(EmployeeSalaryStructureDao structureDao,
                                      EmployeeSalaryComponentDao structureCompDao,
                                      SalaryComponentDao componentDao) {
        this.structureDao = structureDao;
        this.structureCompDao = structureCompDao;
        this.componentDao = componentDao;
    }

    @Override
    public SalaryStructureDtos.StructureResponse upsert(SalaryStructureDtos.UpsertRequest req) {
        // Create new structure version
        EmployeeSalaryStructure es = EmployeeSalaryStructure.builder()
                .employeeId(req.getEmployeeId())
                .ctc(req.getCtc())
                .grossSalary(req.getGrossSalary())
                .effectiveFrom(req.getEffectiveFrom())
                .build();
        es = structureDao.save(es);

        final UUID sid = es.getId();
        for (SalaryStructureDtos.ComponentAmount ca : req.getComponents()) {
            EmployeeSalaryComponent esc = EmployeeSalaryComponent.builder()
                    .salaryStructureId(sid)
                    .componentId(ca.getComponentId())
                    .amount(ca.getAmount())
                    .build();
            structureCompDao.save(esc);
        }
        return mapToResponse(es);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalaryStructureDtos.StructureResponse> getLatestByEmployee(UUID employeeId) {
        return structureDao.findByEmployeeIdOrderByEffectiveFromDesc(employeeId).stream().findFirst()
                .map(this::mapToResponse);
    }

    private SalaryStructureDtos.StructureResponse mapToResponse(EmployeeSalaryStructure es) {
        List<EmployeeSalaryComponent> comps = structureCompDao.findBySalaryStructureId(es.getId());
        List<SalaryStructureDtos.StructureComponentResp> items = comps.stream().map(c -> {
            SalaryComponent sc = componentDao.findById(c.getComponentId())
                    .orElseThrow(() -> new IllegalArgumentException("Salary component not found: " + c.getComponentId()));
            return SalaryStructureDtos.StructureComponentResp.builder()
                    .componentId(sc.getId())
                    .code(sc.getCode())
                    .name(sc.getName())
                    .type(sc.getType())
                    .amount(c.getAmount())
                    .build();
        }).collect(Collectors.toList());
        return SalaryStructureDtos.StructureResponse.builder()
                .id(es.getId())
                .employeeId(es.getEmployeeId())
                .ctc(es.getCtc())
                .grossSalary(es.getGrossSalary())
                .effectiveFrom(es.getEffectiveFrom())
                .components(items)
                .build();
    }
}
