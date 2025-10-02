package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CategoryResponse {

    private Long id;
    private String name;
    private List<ArticleResponse> articles;
}
