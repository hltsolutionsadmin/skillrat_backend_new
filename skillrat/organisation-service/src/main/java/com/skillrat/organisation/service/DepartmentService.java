package com.skillrat.organisation.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.organisation.domain.Department;
import com.skillrat.organisation.repo.DepartmentRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<Department> getAllDepartments(UUID b2bUnitId, Pageable pageable) {
        return departmentRepository.findByB2bUnitId(b2bUnitId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Department> searchDepartments(UUID b2bUnitId, String searchTerm, Pageable pageable) {
        return departmentRepository.findByNameContainingIgnoreCaseAndB2bUnitId(searchTerm, b2bUnitId, pageable);
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
    }

    @Transactional
    public Department createDepartment(Department department) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        
        departmentRepository.findByName(department.getName())
                .ifPresent(d -> {
                    throw new IllegalArgumentException("Department with name " + department.getName() + " already exists");
                });
        
        department.setTenantId(tenantId);
        Department saved = departmentRepository.save(department);
        log.info("Department created id={}, name={}, b2bUnitId={}, tenantId={}", 
                saved.getId(), saved.getName(), saved.getB2bUnitId(), tenantId);
        return saved;
    }

    @Transactional
    public Department updateDepartment(UUID id, Department departmentDetails) {
        Department department = getDepartmentById(id);
        
        if (!department.getName().equals(departmentDetails.getName())) {
            departmentRepository.findByName(departmentDetails.getName())
                    .ifPresent(d -> {
                        throw new IllegalArgumentException("Department with name " + departmentDetails.getName() + " already exists");
                    });
        }
        if (departmentDetails.getCode() != null) {
            department.setCode(departmentDetails.getCode());
        }

        if (departmentDetails.getName() != null) {
            department.setName(departmentDetails.getName());
        }

        if (departmentDetails.getDescription() != null) {
            department.setDescription(departmentDetails.getDescription());
        }
        department.setActive(departmentDetails.isActive());
        
        Department updated = departmentRepository.save(department);
        log.info("Department updated id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    @Transactional
    public void deleteDepartment(UUID id) {
        Department department = getDepartmentById(id);
        departmentRepository.delete(department);
        log.info("Department deactivated id={}, name={}", department.getId(), department.getName());
    }

    @Transactional(readOnly = true)
    public Page<Department> getActiveDepartments(UUID b2bUnitId, Pageable pageable) {
        return departmentRepository.findByActiveAndB2bUnitId(true, b2bUnitId, pageable);
    }
}
