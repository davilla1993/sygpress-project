package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ServiceResponse {

    private String publicId;
    private String name;
    private List<PricingResponse> pricing;
}
