package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.CategoryRequest;
import com.follysitou.sygpress.dto.response.CategoryResponse;
import com.follysitou.sygpress.mapper.CategoryMapper;
import com.follysitou.sygpress.model.Category;
import com.follysitou.sygpress.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        List<Category> categories = categoryService.findAll();
        return ResponseEntity.ok(categoryMapper.toResponseList(categories));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category categoryDetails = new Category();
        categoryDetails.setName(request.getName());

        Category updated = categoryService.update(id, categoryDetails);
        return ResponseEntity.ok(categoryMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
