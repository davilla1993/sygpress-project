package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.request.ArticleRequest;
import com.follysitou.sygpress.dto.response.ArticleResponse;
import com.follysitou.sygpress.mapper.ArticleMapper;
import com.follysitou.sygpress.model.Article;
import com.follysitou.sygpress.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;

    @PostMapping
    public ResponseEntity<ArticleResponse> create(@Valid @RequestBody ArticleRequest request) {
        Article article = new Article();
        article.setName(request.getName());

        Article saved = articleService.create(article, request.getCategoryId());
        return new ResponseEntity<>(articleMapper.toResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getById(@PathVariable Long id) {
        Article article = articleService.findById(id);
        return ResponseEntity.ok(articleMapper.toResponse(article));
    }

    @GetMapping
    public ResponseEntity<List<ArticleResponse>> getAll() {
        List<Article> articles = articleService.findAll();
        return ResponseEntity.ok(articleMapper.toResponseList(articles));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ArticleResponse>> getByCategory(@PathVariable Long categoryId) {
        List<Article> articles = articleService.findByCategory(categoryId);
        return ResponseEntity.ok(articleMapper.toResponseList(articles));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponse> update(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        Article articleDetails = new Article();
        articleDetails.setName(request.getName());

        Article updated = articleService.update(id, articleDetails, request.getCategoryId());
        return ResponseEntity.ok(articleMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
