package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PricingResponse {

    private String publicId;
    private BigDecimal price;
    private ArticleResponse article;
    private ServiceResponse service;
}
