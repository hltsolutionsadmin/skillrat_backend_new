package com.skillrat.expense.web;

import com.skillrat.expense.domain.Expense;
import com.skillrat.expense.service.ExpenseService;
import com.skillrat.expense.web.dto.ExpenseDTO;
import com.skillrat.expense.web.mapper.ExpenseMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExpenseDTO> list() {
        return service.list().stream().map(ExpenseMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> get(@PathVariable UUID id) {
        return service.get(id)
                .map(ExpenseMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> create(@Valid @RequestBody ExpenseDTO in) {
        Expense toSave = ExpenseMapper.applyCreate(new Expense(), in);
        Expense saved = service.create(toSave);
        ExpenseDTO body = ExpenseMapper.toDto(saved);
        return ResponseEntity.created(URI.create("/api/expenses/" + saved.getId())).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> update(@PathVariable UUID id, @Valid @RequestBody ExpenseDTO in) {
        return service.update(id, existing -> {
            ExpenseMapper.applyUpdate(existing, in);
            return existing;
        }).map(updated -> ResponseEntity.ok(ExpenseMapper.toDto(updated)))
          .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = service.delete(id);
        if (!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
