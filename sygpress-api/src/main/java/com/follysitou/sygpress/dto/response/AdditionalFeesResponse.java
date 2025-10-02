package com.follysitou.sygpress.dto.response;

import lombok.Data;

@Data
public class AdditionalFeesResponse {

    private Long id;
    private String title;
    private String description;
    private double amount;
}
