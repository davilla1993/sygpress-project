package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.Article;
import com.follysitou.sygpress.model.Category;
import com.follysitou.sygpress.repository.ArticleRepository;
import com.follysitou.sygpress.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Article create(Article article, String categoryPublicId) {
        if (articleRepository.existsByName(article.getName())) {
            throw new DuplicateResourceException("Article", "nom", article.getName());
        }

        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", "publicId", categoryPublicId));

        article.setCategory(category);
        return articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public Article findByPublicId(String publicId) {
        return articleRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "publicId", publicId));
    }

    @Transactional(readOnly = true)
    public Page<Article> findAll(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> findByCategory(String categoryPublicId, Pageable pageable) {
        return articleRepository.findByCategoryPublicId(categoryPublicId, pageable);
    }

    @Transactional
    public Article update(String publicId, Article articleDetails, String categoryPublicId) {
        Article article = findByPublicId(publicId);

        // Vérifier si le nouveau nom n'est pas déjà utilisé par un autre article
        articleRepository.findByName(articleDetails.getName())
                .ifPresent(existingArticle -> {
                    if (!existingArticle.getPublicId().equals(publicId)) {
                        throw new DuplicateResourceException("Article", "nom", articleDetails.getName());
                    }
                });

        Category category = categoryRepository.findByPublicId(categoryPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", "publicId", categoryPublicId));

        article.setName(articleDetails.getName());
        article.setCategory(category);

        return articleRepository.save(article);
    }

    @Transactional
    public void delete(String publicId) {
        Article article = findByPublicId(publicId);
        articleRepository.delete(article);
    }
}
