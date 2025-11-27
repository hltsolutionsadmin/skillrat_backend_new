package com.skillrat.expense.service;

import com.skillrat.expense.domain.Expense;
import com.skillrat.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExpenseService {
    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Expense> list() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Expense> get(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    public Expense create(Expense e) {
        return repository.save(e);
    }

    @Transactional
    public Optional<Expense> update(UUID id, java.util.function.UnaryOperator<Expense> updater) {
        return repository.findById(id).map(existing -> {
            Expense updated = updater.apply(existing);
            return repository.save(updated);
        });
    }

    @Transactional
    public boolean delete(UUID id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
