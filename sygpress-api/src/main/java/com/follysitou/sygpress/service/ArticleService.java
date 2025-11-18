package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.Article;
import com.follysitou.sygpress.model.Category;
import com.follysitou.sygpress.repository.ArticleRepository;
import com.follysitou.sygpress.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Article create(Article article, Long categoryId) {
        if (articleRepository.existsByName(article.getName())) {
            throw new DuplicateResourceException("Article", "nom", article.getName());
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", "id", categoryId));

        article.setCategory(category);
        return articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Article> findByCategory(Long categoryId) {
        return articleRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Article update(Long id, Article articleDetails, Long categoryId) {
        Article article = findById(id);

        // Vérifier si le nouveau nom n'est pas déjà utilisé par un autre article
        articleRepository.findByName(articleDetails.getName())
                .ifPresent(existingArticle -> {
                    if (!existingArticle.getId().equals(id)) {
                        throw new DuplicateResourceException("Article", "nom", articleDetails.getName());
                    }
                });

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", "id", categoryId));

        article.setName(articleDetails.getName());
        article.setCategory(category);

        return articleRepository.save(article);
    }

    @Transactional
    public void delete(Long id) {
        Article article = findById(id);
        articleRepository.delete(article);
    }
}
