package com.skillrat.user.organisation.web;

import com.skillrat.user.organisation.domain.Department;
import com.skillrat.user.organisation.service.DepartmentService;
import com.skillrat.user.organisation.web.dto.DepartmentDTO;
import com.skillrat.user.organisation.web.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentMapper departmentMapper = DepartmentMapper.INSTANCE;

    @GetMapping
    public ResponseEntity<Page<DepartmentDTO>> getAllDepartments(
            @RequestParam UUID b2bUnitId,
            @PageableDefault(sort = "name", size = 10) Pageable pageable) {
        return ResponseEntity.ok(
            departmentService.getAllDepartments(b2bUnitId, pageable)
                .map(departmentMapper::toDto)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DepartmentDTO>> searchDepartments(
            @RequestParam UUID b2bUnitId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        org.springframework.data.domain.Sort sort =
                "desc".equalsIgnoreCase(direction)
                        ? org.springframework.data.domain.Sort.by(sortBy).descending()
                        : org.springframework.data.domain.Sort.by(sortBy).ascending();
        org.springframework.data.domain.PageRequest pr = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
            departmentService.searchDepartments(b2bUnitId, query, pr)
                .map(departmentMapper::toDto)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<Page<DepartmentDTO>> getActiveDepartments(
            @RequestParam UUID b2bUnitId,
            @PageableDefault(sort = "name", size = 10) Pageable pageable) {
        return ResponseEntity.ok(
            departmentService.getActiveDepartments(b2bUnitId, pageable)
                .map(departmentMapper::toDto)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable UUID id) {
        Department department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(departmentMapper.toDto(department));
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> createDepartment(@RequestBody DepartmentDTO departmentDTO) {
        Department created = departmentService.createDepartment(departmentDTO);
        DepartmentDTO createdDTO = departmentMapper.toDto(created);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(createdDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable UUID id,
            @RequestBody DepartmentDTO departmentDTO) {
        departmentDTO.setId(id);
        Department updated = departmentService.updateDepartment(id, departmentDTO);
        return ResponseEntity.ok(departmentMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
