package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.request.CategoryRequest;
import com.follysitou.sygpress.dto.response.ArticleResponse;
import com.follysitou.sygpress.dto.response.CategoryResponse;
import com.follysitou.sygpress.model.Article;
import com.follysitou.sygpress.model.Category;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        return category;
    }

    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());

        if (category.getArticles() != null) {
            response.setArticles(category.getArticles().stream()
                    .map(this::articleToMinimalResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private ArticleResponse articleToMinimalResponse(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId());
        response.setName(article.getName());
        // On ne remonte pas la catégorie pour éviter la récursion
        return response;
    }
}
