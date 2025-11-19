package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CategoryResponse {

    private String publicId;
    private String name;
    private int articleCount;
    private List<ArticleResponse> articles;
}
