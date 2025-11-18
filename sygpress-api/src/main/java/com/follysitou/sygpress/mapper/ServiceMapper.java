package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.request.ServiceRequest;
import com.follysitou.sygpress.dto.response.PricingResponse;
import com.follysitou.sygpress.dto.response.ServiceResponse;
import com.follysitou.sygpress.model.Pricing;
import com.follysitou.sygpress.model.LaundryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceMapper {

    public List<ServiceResponse> toResponseList(List<LaundryService> services) {
        return services.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LaundryService toEntity(ServiceRequest request) {
        LaundryService service = new LaundryService();
        service.setName(request.getName());
        return service;
    }

    public ServiceResponse toResponse(LaundryService service) {
        ServiceResponse response = new ServiceResponse();
        response.setPublicId(service.getPublicId());
        response.setName(service.getName());

        if (service.getPricing() != null) {
            response.setPricing(service.getPricing().stream()
                    .map(this::pricingToMinimalResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private PricingResponse pricingToMinimalResponse(Pricing pricing) {
        PricingResponse response = new PricingResponse();
        response.setPublicId(pricing.getPublicId());
        response.setPrice(pricing.getPrice());
        return response;
    }
}