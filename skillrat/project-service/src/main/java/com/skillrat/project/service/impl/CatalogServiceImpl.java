package com.skillrat.project.service.impl;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.project.domain.IncidentCategoryEntity;
import com.skillrat.project.domain.IncidentSubCategoryEntity;
import com.skillrat.project.repo.IncidentCategoryRepository;
import com.skillrat.project.repo.IncidentSubCategoryRepository;
import com.skillrat.project.service.CatalogService;
import com.skillrat.project.service.dto.CategoryDTO;
import com.skillrat.project.service.dto.SubCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class CatalogServiceImpl implements CatalogService {

    private final IncidentCategoryRepository categoryRepo;
    private final IncidentSubCategoryRepository subCategoryRepo;

    public CatalogServiceImpl(IncidentCategoryRepository categoryRepo,
                              IncidentSubCategoryRepository subCategoryRepo) {
        this.categoryRepo = categoryRepo;
        this.subCategoryRepo = subCategoryRepo;
    }

    private static CategoryDTO toDTO(IncidentCategoryEntity e) {
        CategoryDTO dto = new CategoryDTO();
        dto.id = e.getId();
        dto.organisationId = e.getOrganisationId();
        dto.code = e.getCode();
        dto.name = e.getName();
        dto.active = e.isActive();
        return dto;
    }

    private static void apply(IncidentCategoryEntity e, CategoryDTO dto) {
        if (dto.code != null) e.setCode(dto.code.trim());
        if (dto.name != null) e.setName(dto.name.trim());
        if (dto.active != null) e.setActive(dto.active);
    }

    private static SubCategoryDTO toDTO(IncidentSubCategoryEntity e) {
        SubCategoryDTO dto = new SubCategoryDTO();
        dto.id = e.getId();
        dto.categoryId = e.getCategory().getId();
        dto.code = e.getCode();
        dto.name = e.getName();
        dto.active = e.isActive();
        return dto;
    }

    private static void apply(IncidentSubCategoryEntity e, SubCategoryDTO dto) {
        if (dto.code != null) e.setCode(dto.code.trim());
        if (dto.name != null) e.setName(dto.name.trim());
        if (dto.active != null) e.setActive(dto.active);
    }

    // Category
    @Override
    public CategoryDTO createCategory(CategoryDTO dto) {
        String tenantId = TenantContext.getTenantId();
        if (dto.code == null || dto.code.isBlank()) throw new IllegalArgumentException("code is required");
        if (dto.name == null || dto.name.isBlank()) throw new IllegalArgumentException("name is required");
        if (dto.organisationId == null) throw new IllegalArgumentException("organisationId is required");
        if (categoryRepo.existsByTenantIdAndOrganisationIdAndCodeIgnoreCase(tenantId, dto.organisationId, dto.code)) {
            throw new IllegalArgumentException("Category code already exists");
        }
        IncidentCategoryEntity e = new IncidentCategoryEntity();
        e.setTenantId(tenantId);
        e.setOrganisationId(dto.organisationId);
        e.setCode(dto.code.trim());
        e.setName(dto.name.trim());
        e.setActive(dto.active == null ? true : dto.active);
        return toDTO(categoryRepo.save(e));
    }

    @Override
    public CategoryDTO updateCategory(UUID id, CategoryDTO dto) {
        String tenantId = TenantContext.getTenantId();
        IncidentCategoryEntity e = categoryRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
        UUID orgId = e.getOrganisationId();
        if (dto.code != null && categoryRepo.existsByTenantIdAndOrganisationIdAndCodeIgnoreCaseAndIdNot(tenantId, orgId, dto.code, id)) {
            throw new IllegalArgumentException("Category code already exists");
        }
        apply(e, dto);
        return toDTO(categoryRepo.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategory(UUID id) {
        String tenantId = TenantContext.getTenantId();
        return categoryRepo.findByIdAndTenantId(id, tenantId)
                .map(CatalogServiceImpl::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategories(UUID organisationId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        if (organisationId == null) {
            throw new IllegalArgumentException("organisationId is required");
        }
        return categoryRepo.findAllByTenantIdAndOrganisationId(tenantId, organisationId, pageable).map(CatalogServiceImpl::toDTO);
    }

    @Override
    public void deleteCategory(UUID id) {
        String tenantId = TenantContext.getTenantId();
        IncidentCategoryEntity e = categoryRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
        categoryRepo.delete(e);
    }

    // SubCategory
    @Override
    public SubCategoryDTO createSubCategory(SubCategoryDTO dto) {
        String tenantId = TenantContext.getTenantId();
        if (dto.categoryId == null) throw new IllegalArgumentException("categoryId is required");
        if (dto.code == null || dto.code.isBlank()) throw new IllegalArgumentException("code is required");
        if (dto.name == null || dto.name.isBlank()) throw new IllegalArgumentException("name is required");
        IncidentCategoryEntity category = categoryRepo.findByIdAndTenantId(dto.categoryId, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
        if (subCategoryRepo.existsByTenantIdAndCategory_IdAndCodeIgnoreCase(tenantId, category.getId(), dto.code)) {
            throw new IllegalArgumentException("SubCategory code already exists for this category");
        }
        IncidentSubCategoryEntity e = new IncidentSubCategoryEntity();
        e.setTenantId(tenantId);
        e.setCategory(category);
        e.setCode(dto.code.trim());
        e.setName(dto.name.trim());
        e.setActive(dto.active == null ? true : dto.active);
        return toDTO(subCategoryRepo.save(e));
    }

    @Override
    public SubCategoryDTO updateSubCategory(UUID id, SubCategoryDTO dto) {
        String tenantId = TenantContext.getTenantId();
        IncidentSubCategoryEntity e = subCategoryRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NoSuchElementException("SubCategory not found"));
        UUID categoryId = e.getCategory().getId();
        if (dto.categoryId != null && !dto.categoryId.equals(categoryId)) {
            // move to a different category
            IncidentCategoryEntity newCat = categoryRepo.findByIdAndTenantId(dto.categoryId, tenantId)
                    .orElseThrow(() -> new NoSuchElementException("Category not found"));
            categoryId = newCat.getId();
            e.setCategory(newCat);
        }
        if (dto.code != null && subCategoryRepo.existsByTenantIdAndCategory_IdAndCodeIgnoreCaseAndIdNot(tenantId, categoryId, dto.code, id)) {
            throw new IllegalArgumentException("SubCategory code already exists for this category");
        }
        apply(e, dto);
        return toDTO(subCategoryRepo.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public SubCategoryDTO getSubCategory(UUID id) {
        String tenantId = TenantContext.getTenantId();
        return subCategoryRepo.findByIdAndTenantId(id, tenantId)
                .map(CatalogServiceImpl::toDTO)
                .orElseThrow(() -> new NoSuchElementException("SubCategory not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubCategoryDTO> getAllSubCategories(UUID categoryId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        return subCategoryRepo.findAllByTenantIdAndCategory_Id(tenantId, categoryId, pageable)
                .map(CatalogServiceImpl::toDTO);
    }

    @Override
    public void deleteSubCategory(UUID id) {
        String tenantId = TenantContext.getTenantId();
        var e = subCategoryRepo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NoSuchElementException("SubCategory not found"));
        subCategoryRepo.delete(e);
    }
}
