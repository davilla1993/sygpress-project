package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.request.ArticleRequest;
import com.follysitou.sygpress.dto.response.ArticleResponse;
import com.follysitou.sygpress.dto.response.CategoryResponse;
import com.follysitou.sygpress.model.Article;
import com.follysitou.sygpress.model.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArticleMapper {

    public List<ArticleResponse> toResponseList(List<Article> articles) {
        return articles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Article toEntity(ArticleRequest request) {
        Article article = new Article();
        article.setName(request.getName());
        return article;
    }

    public ArticleResponse toResponse(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId());
        response.setName(article.getName());

        if (article.getCategory() != null) {
            response.setCategory(categoryToMinimalResponse(article.getCategory()));
        }

        return response;
    }

    private CategoryResponse categoryToMinimalResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        // On exclut les articles pour éviter la récursion
        return response;
    }
}
