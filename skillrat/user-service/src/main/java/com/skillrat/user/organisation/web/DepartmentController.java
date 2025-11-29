package com.skillrat.user.organisation.web;

import com.skillrat.user.organisation.domain.Department;
import com.skillrat.user.organisation.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<Page<Department>> getAllDepartments(
            @RequestParam UUID b2bUnitId,
            @PageableDefault(sort = "name", size = 10) Pageable pageable) {
        return ResponseEntity.ok(departmentService.getAllDepartments(b2bUnitId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Department>> searchDepartments(
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
        return ResponseEntity.ok(departmentService.searchDepartments(b2bUnitId, query, pr));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<Department>> getActiveDepartments(
            @RequestParam UUID b2bUnitId,
            @PageableDefault(sort = "name", size = 10) Pageable pageable) {
        return ResponseEntity.ok(departmentService.getActiveDepartments(b2bUnitId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable @NonNull UUID id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        Department created = departmentService.createDepartment(department);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable @NonNull UUID id,
            @RequestBody Department department) {
        department.setId(id);
        return ResponseEntity.ok(departmentService.updateDepartment(id, department));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable @NonNull UUID id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
