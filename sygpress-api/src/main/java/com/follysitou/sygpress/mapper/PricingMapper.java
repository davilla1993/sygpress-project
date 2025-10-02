package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.request.PricingRequest;
import com.follysitou.sygpress.dto.response.ArticleResponse;
import com.follysitou.sygpress.dto.response.PricingResponse;
import com.follysitou.sygpress.dto.response.ServiceResponse;
import com.follysitou.sygpress.model.Article;
import com.follysitou.sygpress.model.Pricing;
import com.follysitou.sygpress.model.Service;
import org.springframework.stereotype.Component;

@Component
public class PricingMapper {

    public Pricing toEntity(PricingRequest request) {
        Pricing pricing = new Pricing();
        pricing.setPrice(request.getPrice());
        return pricing;
    }

    public PricingResponse toResponse(Pricing pricing) {
        PricingResponse response = new PricingResponse();
        response.setId(pricing.getId());
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
        response.setId(article.getId());
        response.setName(article.getName());
        return response;
    }

    private ServiceResponse serviceToMinimalResponse(Service service) {
        ServiceResponse response = new ServiceResponse();
        response.setId(service.getId());
        response.setName(service.getName());
        return response;
    }
}
