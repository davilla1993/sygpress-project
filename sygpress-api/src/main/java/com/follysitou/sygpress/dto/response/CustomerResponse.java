package com.follysitou.sygpress.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerResponse {

    private String publicId;
    private String name;
    private String phoneNumber;
    private String address;
    private LocalDateTime createdAt;
    private List<InvoiceResponse> invoices;
}
