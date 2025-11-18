package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.CategoryRequest;
import com.follysitou.sygpress.dto.response.CategoryResponse;
import com.follysitou.sygpress.mapper.CategoryMapper;
import com.follysitou.sygpress.model.Category;
import com.follysitou.sygpress.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());

        Category saved = categoryService.create(category);
        return new ResponseEntity<>(categoryMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<CategoryResponse> getByPublicId(@PathVariable String publicId) {
        Category category = categoryService.findByPublicId(publicId);
        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAll(Pageable pageable) {
        Page<Category> categories = categoryService.findAll(pageable);
        return ResponseEntity.ok(categories.map(categoryMapper::toResponse));
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<CategoryResponse> update(@PathVariable String publicId, @Valid @RequestBody CategoryRequest request) {
        Category categoryDetails = new Category();
        categoryDetails.setName(request.getName());

        Category updated = categoryService.update(publicId, categoryDetails);
        return ResponseEntity.ok(categoryMapper.toResponse(updated));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> delete(@PathVariable String publicId) {
        categoryService.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}
