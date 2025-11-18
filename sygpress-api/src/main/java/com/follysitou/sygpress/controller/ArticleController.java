package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.ArticleRequest;
import com.follysitou.sygpress.dto.response.ArticleResponse;
import com.follysitou.sygpress.mapper.ArticleMapper;
import com.follysitou.sygpress.model.Article;
import com.follysitou.sygpress.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArticleResponse> create(@Valid @RequestBody ArticleRequest request) {
        Article article = new Article();
        article.setName(request.getName());

        Article saved = articleService.create(article, request.getCategoryPublicId());
        return new ResponseEntity<>(articleMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArticleResponse> getByPublicId(@PathVariable String publicId) {
        Article article = articleService.findByPublicId(publicId);
        return ResponseEntity.ok(articleMapper.toResponse(article));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ArticleResponse>> getAll(Pageable pageable) {
        Page<Article> articles = articleService.findAll(pageable);
        return ResponseEntity.ok(articles.map(articleMapper::toResponse));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/category/{categoryPublicId}")
    public ResponseEntity<Page<ArticleResponse>> getByCategory(@PathVariable String categoryPublicId, Pageable pageable) {
        Page<Article> articles = articleService.findByCategory(categoryPublicId, pageable);
        return ResponseEntity.ok(articles.map(articleMapper::toResponse));
    }


    @PutMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArticleResponse> update(@PathVariable String publicId, @Valid @RequestBody ArticleRequest request) {
        Article articleDetails = new Article();
        articleDetails.setName(request.getName());

        Article updated = articleService.update(publicId, articleDetails, request.getCategoryPublicId());
        return ResponseEntity.ok(articleMapper.toResponse(updated));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String publicId) {
        articleService.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}
