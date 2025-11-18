package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ArticleResponse {

    private String publicId;
    private String name;
    private CategoryResponse category;
    private List<PricingResponse> pricing;
}
