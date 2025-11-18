package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.Category;
import com.follysitou.sygpress.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Category findByPublicId(String publicId) {
        return categoryRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", "publicId", publicId));
    }

    @Transactional(readOnly = true)
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Transactional
    public Category update(String publicId, Category categoryDetails) {
        Category category = findByPublicId(publicId);

        // Vérifier si le nouveau nom n'est pas déjà utilisé par une autre catégorie
        categoryRepository.findByName(categoryDetails.getName())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getPublicId().equals(publicId)) {
                        throw new DuplicateResourceException("Catégorie", "nom", categoryDetails.getName());
                    }
                });

        category.setName(categoryDetails.getName());

        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(String publicId) {
        Category category = findByPublicId(publicId);
        categoryRepository.delete(category);
    }
}
