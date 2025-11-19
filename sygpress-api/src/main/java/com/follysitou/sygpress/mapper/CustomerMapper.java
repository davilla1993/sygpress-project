package com.follysitou.sygpress.mapper;

import com.follysitou.sygpress.dto.request.CustomerRequest;
import com.follysitou.sygpress.dto.response.CustomerResponse;
import com.follysitou.sygpress.dto.response.InvoiceResponse;
import com.follysitou.sygpress.model.Customer;
import com.follysitou.sygpress.model.Invoice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerMapper {

    public List<CustomerResponse> toResponseList(List<Customer> customers) {
        return customers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        return customer;
    }

    public CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setPublicId(customer.getPublicId());
        response.setName(customer.getName());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setAddress(customer.getAddress());
        response.setCreatedAt(customer.getCreatedAt());

        if (customer.getInvoices() != null) {
            response.setInvoices(customer.getInvoices().stream()
                    .map(this::invoiceToMinimalResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    // Méthode pour une réponse minimale (évite la récursion)
    private InvoiceResponse invoiceToMinimalResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setPublicId(invoice.getPublicId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setDepositDate(invoice.getDepositDate());
        response.setDeliveryDate(invoice.getDeliveryDate());
        response.setInvoicePaid(invoice.isInvoicePaid());
        response.setProcessingStatus(invoice.getProcessingStatus());
        // On ne remonte pas le customer pour éviter la boucle infinie
        return response;
    }
}
