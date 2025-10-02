package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CustomerResponse {

    private Long id;
    private String name;
    private String phoneNumber;
    private String address;
    private List<InvoiceResponse> invoices;
}
