package com.skillrat.project.service;

import com.skillrat.project.service.dto.CategoryDTO;
import com.skillrat.project.service.dto.SubCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CatalogService {
    // Category
    CategoryDTO createCategory(CategoryDTO dto);
    CategoryDTO updateCategory(UUID id, CategoryDTO dto);
    CategoryDTO getCategory(UUID id);
    Page<CategoryDTO> getAllCategories(UUID organisationId, Pageable pageable);
    void deleteCategory(UUID id);

    // SubCategory
    SubCategoryDTO createSubCategory(SubCategoryDTO dto);
    SubCategoryDTO updateSubCategory(UUID id, SubCategoryDTO dto);
    SubCategoryDTO getSubCategory(UUID id);
    Page<SubCategoryDTO> getAllSubCategories(UUID categoryId, Pageable pageable);
    void deleteSubCategory(UUID id);
}
