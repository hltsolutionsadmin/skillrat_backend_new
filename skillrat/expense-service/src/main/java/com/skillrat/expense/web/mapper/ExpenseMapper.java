package com.skillrat.expense.web.mapper;

import com.skillrat.expense.domain.Expense;
import com.skillrat.expense.web.dto.ExpenseDTO;

public class ExpenseMapper {

    public static ExpenseDTO toDto(Expense e) {
        if (e == null) return null;
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(e.getId());
        dto.setAmount(e.getAmount());
        dto.setCurrency(e.getCurrency());
        dto.setCategory(e.getCategory());
        dto.setStatus(e.getStatus());
        dto.setDescription(e.getDescription());
        dto.setIncurredDate(e.getIncurredDate());
        dto.setCreatedDate(e.getCreatedDate());
        dto.setUpdatedDate(e.getUpdatedDate());
        return dto;
    }

    public static Expense applyCreate(Expense target, ExpenseDTO src) {
        if (target == null) target = new Expense();
        if (src == null) return target;
        // Only map allowed fields from client; do NOT map id/created/updated/tenant
        target.setAmount(src.getAmount());
        target.setCurrency(src.getCurrency());
        target.setCategory(src.getCategory());
        target.setStatus(src.getStatus());
        target.setDescription(src.getDescription());
        target.setIncurredDate(src.getIncurredDate());
        return target;
    }

    public static Expense applyUpdate(Expense target, ExpenseDTO src) {
        // Same mapping rules as create; keep server-controlled fields intact
        return applyCreate(target, src);
    }
}
