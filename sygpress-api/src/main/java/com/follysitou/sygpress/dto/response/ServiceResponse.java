package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ServiceResponse {

    private Long id;
    private String name;
    private List<PricingResponse> pricing;
}
