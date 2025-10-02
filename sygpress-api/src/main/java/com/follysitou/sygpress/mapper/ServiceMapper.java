package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.request.ServiceRequest;
import com.follysitou.sygpress.dto.response.PricingResponse;
import com.follysitou.sygpress.dto.response.ServiceResponse;
import com.follysitou.sygpress.model.Pricing;
import com.follysitou.sygpress.model.Service;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ServiceMapper {

    public Service toEntity(ServiceRequest request) {
        Service service = new Service();
        service.setName(request.getName());
        return service;
    }

    public ServiceResponse toResponse(Service service) {
        ServiceResponse response = new ServiceResponse();
        response.setId(service.getId());
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
        response.setId(pricing.getId());
        response.setPrice(pricing.getPrice());
        return response;
    }
}