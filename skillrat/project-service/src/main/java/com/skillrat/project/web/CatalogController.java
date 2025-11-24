package com.skillrat.project.web;

import com.skillrat.project.service.CatalogService;
import com.skillrat.project.service.dto.CategoryDTO;
import com.skillrat.project.service.dto.SubCategoryDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/catalog")
@Validated
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // Category endpoints
    @PostMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public CategoryDTO createCategory(@RequestBody @Valid CategoryDTO dto) {
        return catalogService.createCategory(dto);
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("isAuthenticated()")
    public CategoryDTO updateCategory(@PathVariable("id") UUID id,
                                      @RequestBody @Valid CategoryDTO dto) {
        return catalogService.updateCategory(id, dto);
    }

    @GetMapping("/categories/{id}")
    @PreAuthorize("isAuthenticated()")
    public CategoryDTO getCategory(@PathVariable("id") UUID id) {
        return catalogService.getCategory(id);
    }

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public Page<CategoryDTO> getAllCategories(@RequestParam("organisationId") UUID organisationId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return catalogService.getAllCategories(organisationId, pageable);
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteCategory(@PathVariable("id") UUID id) {
        catalogService.deleteCategory(id);
    }

    // SubCategory endpoints
    @PostMapping("/categories/{categoryId}/subcategories")
    @PreAuthorize("isAuthenticated()")
    public SubCategoryDTO createSubCategory(@PathVariable("categoryId") UUID categoryId,
                                            @RequestBody @Valid SubCategoryDTO dto) {
        dto.categoryId = categoryId;
        return catalogService.createSubCategory(dto);
    }

    @PutMapping("/subcategories/{id}")
    @PreAuthorize("isAuthenticated()")
    public SubCategoryDTO updateSubCategory(@PathVariable("id") UUID id,
                                            @RequestBody @Valid SubCategoryDTO dto) {
        return catalogService.updateSubCategory(id, dto);
    }

    @GetMapping("/subcategories/{id}")
    @PreAuthorize("isAuthenticated()")
    public SubCategoryDTO getSubCategory(@PathVariable("id") UUID id) {
        return catalogService.getSubCategory(id);
    }

    @GetMapping("/categories/{categoryId}/subcategories")
    @PreAuthorize("isAuthenticated()")
    public Page<SubCategoryDTO> getAllSubCategories(@PathVariable("categoryId") UUID categoryId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return catalogService.getAllSubCategories(categoryId, pageable);
    }

    @DeleteMapping("/subcategories/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteSubCategory(@PathVariable("id") UUID id) {
        catalogService.deleteSubCategory(id);
    }
}
