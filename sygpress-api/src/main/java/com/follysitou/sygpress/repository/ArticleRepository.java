package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByPublicId(String publicId);
    Optional<Article> findByName(String name);
    List<Article> findByCategoryId(Long categoryId);
    Page<Article> findByCategoryPublicId(String categoryPublicId, Pageable pageable);
    boolean existsByName(String name);
}
