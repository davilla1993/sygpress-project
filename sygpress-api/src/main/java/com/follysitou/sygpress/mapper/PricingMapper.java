package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.request.PricingRequest;
import com.follysitou.sygpress.dto.response.ArticleResponse;
import com.follysitou.sygpress.dto.response.PricingResponse;
import com.follysitou.sygpress.dto.response.ServiceResponse;
import com.follysitou.sygpress.model.Article;
import com.follysitou.sygpress.model.Pricing;
import com.follysitou.sygpress.model.LaundryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PricingMapper {

    public List<PricingResponse> toResponseList(List<Pricing> pricings) {
        return pricings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Pricing toEntity(PricingRequest request) {
        Pricing pricing = new Pricing();
        pricing.setPrice(request.getPrice());
        return pricing;
    }

    public PricingResponse toResponse(Pricing pricing) {
        PricingResponse response = new PricingResponse();
        response.setPublicId(pricing.getPublicId());
        response.setPrice(pricing.getPrice());

        if (pricing.getArticle() != null) {
            response.setArticle(articleToMinimalResponse(pricing.getArticle()));
        }

        if (pricing.getService() != null) {
            response.setService(serviceToMinimalResponse(pricing.getService()));
        }

        return response;
    }

    private ArticleResponse articleToMinimalResponse(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.setPublicId(article.getPublicId());
        response.setName(article.getName());
        return response;
    }

    private ServiceResponse serviceToMinimalResponse(LaundryService service) {
        ServiceResponse response = new ServiceResponse();
        response.setPublicId(service.getPublicId());
        response.setName(service.getName());
        return response;
    }
}
