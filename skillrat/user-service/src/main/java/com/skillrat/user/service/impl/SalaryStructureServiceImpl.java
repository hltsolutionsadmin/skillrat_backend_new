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
        com.skillrat.user.domain.Employee empRef = new com.skillrat.user.domain.Employee();
        empRef.setId(req.getEmployeeId());
        EmployeeSalaryStructure es = EmployeeSalaryStructure.builder()
                .employee(empRef)
                .ctc(req.getCtc())
                .grossSalary(req.getGrossSalary())
                .effectiveFrom(req.getEffectiveFrom())
                .build();
        es = structureDao.save(es);

        final EmployeeSalaryStructure structRef = es;
        for (SalaryStructureDtos.ComponentAmount ca : req.getComponents()) {
            SalaryComponent comp = componentDao.findById(ca.getComponentId())
                    .orElseThrow(() -> new IllegalArgumentException("Salary component not found: " + ca.getComponentId()));
            EmployeeSalaryComponent esc = EmployeeSalaryComponent.builder()
                    .salaryStructure(structRef)
                    .component(comp)
                    .amount(ca.getAmount())
                    .build();
            structureCompDao.save(esc);
        }
        return mapToResponse(es);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalaryStructureDtos.StructureResponse> getLatestByEmployee(UUID employeeId) {
        return structureDao.findByEmployee_IdOrderByEffectiveFromDesc(employeeId).stream().findFirst()
                .map(this::mapToResponse);
    }

    private SalaryStructureDtos.StructureResponse mapToResponse(EmployeeSalaryStructure es) {
        List<EmployeeSalaryComponent> comps = structureCompDao.findBySalaryStructure_Id(es.getId());
        List<SalaryStructureDtos.StructureComponentResp> items = comps.stream().map(c -> {
            SalaryComponent sc = c.getComponent();
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
                .employeeId(es.getEmployee().getId())
                .ctc(es.getCtc())
                .grossSalary(es.getGrossSalary())
                .effectiveFrom(es.getEffectiveFrom())
                .components(items)
                .build();
    }
}
