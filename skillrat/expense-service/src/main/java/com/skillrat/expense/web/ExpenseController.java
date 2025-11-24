package com.skillrat.expense.web;

import com.skillrat.expense.domain.Expense;
import com.skillrat.expense.repository.ExpenseRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseRepository repo;

    public ExpenseController(ExpenseRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Expense> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> get(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Expense> create(@Valid @RequestBody Expense in) {
        Expense saved = repo.save(in);
        return ResponseEntity.created(URI.create("/api/expenses/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> update(@PathVariable UUID id, @Valid @RequestBody Expense in) {
        return repo.findById(id).map(existing -> {
            existing.setAmount(in.getAmount());
            existing.setCurrency(in.getCurrency());
            existing.setCategory(in.getCategory());
            existing.setStatus(in.getStatus());
            existing.setDescription(in.getDescription());
            existing.setIncurredDate(in.getIncurredDate());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
