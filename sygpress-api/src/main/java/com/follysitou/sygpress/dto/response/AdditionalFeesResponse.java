package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdditionalFeesResponse {

    private String publicId;
    private String title;
    private String description;
    private BigDecimal amount;
}
