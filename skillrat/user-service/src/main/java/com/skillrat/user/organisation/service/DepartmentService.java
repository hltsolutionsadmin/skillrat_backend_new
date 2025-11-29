package com.skillrat.user.organisation.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.organisation.domain.Department;
import com.skillrat.user.organisation.repo.B2BUnitRepository;
import com.skillrat.user.organisation.repo.DepartmentRepository;
import com.skillrat.user.organisation.web.dto.DepartmentDTO;
import com.skillrat.user.organisation.web.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);
    private final DepartmentRepository departmentRepository;
    private final B2BUnitRepository b2bUnitRepository;
    private final DepartmentMapper departmentMapper = DepartmentMapper.INSTANCE;

    @Transactional(readOnly = true)
    public Page<Department> getAllDepartments(UUID b2bUnitId, Pageable pageable) {
        // Verify the B2B unit exists
        if (!b2bUnitRepository.existsById(b2bUnitId)) {
            throw new IllegalArgumentException("B2B Unit not found with id: " + b2bUnitId);
        }
        return departmentRepository.findByB2bUnitId(b2bUnitId, pageable);
    }

    @Transactional
    public Department createDepartment(DepartmentDTO departmentDTO) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        
        // Check if department with same name already exists
        departmentRepository.findByName(departmentDTO.getName())
                .ifPresent(d -> {
                    throw new IllegalArgumentException("Department with name " + departmentDTO.getName() + " already exists");
                });

        // Check if department with same code already exists
        if (departmentDTO.getCode() != null) {
            departmentRepository.findByCode(departmentDTO.getCode())
                    .ifPresent(d -> {
                        throw new IllegalArgumentException("Department with code " + departmentDTO.getCode() + " already exists");
                    });
        }

        // Convert DTO to entity
        Department department = departmentMapper.toEntity(departmentDTO);
        department.setTenantId(tenantId);

        // Save the department
        Department savedDepartment = departmentRepository.save(department);
        
        // If b2bUnitId is provided, establish the relationship
        if (departmentDTO.getB2bUnitId() != null) {
            B2BUnit b2bUnit = b2bUnitRepository.findById(departmentDTO.getB2bUnitId())
                    .orElseThrow(() -> new IllegalArgumentException("B2B Unit not found with id: " + departmentDTO.getB2bUnitId()));
            
            // Use the helper method to manage the bidirectional relationship
            savedDepartment.addB2BUnit(b2bUnit);
            savedDepartment = departmentRepository.save(savedDepartment);
        }

        log.info("Department created id={}, name={}, tenantId={}", 
                savedDepartment.getId(), savedDepartment.getName(), tenantId);
        return savedDepartment;
    }

    @Transactional
    public Department updateDepartment(UUID id, DepartmentDTO departmentDTO) {
        Department existingDepartment = getDepartmentById(id);
        
        // Check if name is being changed and if the new name already exists
        if (!existingDepartment.getName().equals(departmentDTO.getName())) {
            departmentRepository.findByName(departmentDTO.getName())
                    .ifPresent(d -> {
                        throw new IllegalArgumentException("Department with name " + departmentDTO.getName() + " already exists");
                    });
        }

        // Update fields from DTO
        if (departmentDTO.getName() != null) {
            existingDepartment.setName(departmentDTO.getName());
        }
        if (departmentDTO.getDescription() != null) {
            existingDepartment.setDescription(departmentDTO.getDescription());
        }
        if (departmentDTO.getCode() != null) {
            existingDepartment.setCode(departmentDTO.getCode());
        }
        existingDepartment.setActive(departmentDTO.isActive());

        // Handle B2B Unit relationship if changed
        if (departmentDTO.getB2bUnitId() != null) {
            B2BUnit b2bUnit = b2bUnitRepository.findById(departmentDTO.getB2bUnitId())
                    .orElseThrow(() -> new IllegalArgumentException("B2B Unit not found with id: " + departmentDTO.getB2bUnitId()));
            
            // Clear existing relationships
            existingDepartment.getB2bUnits().forEach(unit -> 
                unit.getDepartments().remove(existingDepartment)
            );
            existingDepartment.getB2bUnits().clear();
            
            // Add the new relationship
            existingDepartment.addB2BUnit(b2bUnit);
        }

        Department updated = departmentRepository.save(existingDepartment);
        log.info("Department updated id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
    }

    @Transactional
    public void deleteDepartment(UUID id) {
        Department department = getDepartmentById(id);
        
        // Remove department from all B2B Units
        department.getB2bUnits().forEach(unit -> 
            unit.getDepartments().remove(department)
        );
        department.getB2bUnits().clear();
        
        departmentRepository.delete(department);
        log.info("Department deleted id={}, name={}", department.getId(), department.getName());
    }

    @Transactional(readOnly = true)
    public Page<Department> getActiveDepartments(UUID b2bUnitId, Pageable pageable) {
        return departmentRepository.findByActiveAndB2bUnitId(true, b2bUnitId, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Department> searchDepartments(UUID b2bUnitId, String searchTerm, Pageable pageable) {
        if (!b2bUnitRepository.existsById(b2bUnitId)) {
            throw new IllegalArgumentException("B2B Unit not found with id: " + b2bUnitId);
        }
        return departmentRepository.findByNameContainingIgnoreCaseAndB2bUnitId(searchTerm, b2bUnitId, pageable);
    }
}
