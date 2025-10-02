package com.follysitou.sygpress.dto.response;

import lombok.Data;

@Data
public class PricingResponse {

    private Long id;
    private Double price;
    private ArticleResponse article;
    private ServiceResponse service;
}
