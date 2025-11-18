package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.Category;
import com.follysitou.sygpress.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category create(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new DuplicateResourceException("Catégorie", "nom", category.getName());
        }
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category update(Long id, Category categoryDetails) {
        Category category = findById(id);

        // Vérifier si le nouveau nom n'est pas déjà utilisé par une autre catégorie
        categoryRepository.findByName(categoryDetails.getName())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(id)) {
                        throw new DuplicateResourceException("Catégorie", "nom", categoryDetails.getName());
                    }
                });

        category.setName(categoryDetails.getName());

        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }
}
