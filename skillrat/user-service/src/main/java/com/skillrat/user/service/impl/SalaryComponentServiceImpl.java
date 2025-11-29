package com.skillrat.user.service.impl;

import com.skillrat.user.repo.SalaryComponentDao;
import com.skillrat.user.dto.SalaryComponentDto;
import com.skillrat.user.domain.SalaryComponent;
import com.skillrat.user.service.SalaryComponentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SalaryComponentServiceImpl implements SalaryComponentService {

    private final SalaryComponentDao dao;

    public SalaryComponentServiceImpl(SalaryComponentDao dao) {
        this.dao = dao;
    }

    @Override
    public SalaryComponentDto create(SalaryComponentDto dto) {
        dao.findByCodeIgnoreCase(dto.getCode()).ifPresent(x -> { throw new IllegalArgumentException("Component code already exists"); });
        SalaryComponent sc = SalaryComponent.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .type(dto.getType())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .build();
        sc = dao.save(sc);
        dto.setId(sc.getId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryComponentDto> list() {
        return dao.findAll().stream().map(sc -> SalaryComponentDto.builder()
                .id(sc.getId())
                .name(sc.getName())
                .code(sc.getCode())
                .type(sc.getType())
                .description(sc.getDescription())
                .amount(sc.getAmount())
                .build()).collect(Collectors.toList());
    }

    @Override
    public SalaryComponentDto update(UUID id, SalaryComponentDto dto) {
        SalaryComponent sc = dao.findById(id).orElseThrow(() -> new IllegalArgumentException("Salary component not found"));
if (dto.getName() != null) sc.setName(dto.getName());
        if (dto.getCode() != null) sc.setCode(dto.getCode());
        if (dto.getType() != null) sc.setType(dto.getType());
        if (dto.getDescription() != null) sc.setDescription(dto.getDescription());
        if (dto.getAmount() != null) sc.setAmount(dto.getAmount());
        dao.save(sc);
        return SalaryComponentDto.builder()
                .id(sc.getId())
                .name(sc.getName())
                .code(sc.getCode())
                .type(sc.getType())
                .description(sc.getDescription())
                .amount(sc.getAmount())
                .build();
    }

    @Override
    public void delete(UUID id) {
        dao.deleteById(id);
    }
}
