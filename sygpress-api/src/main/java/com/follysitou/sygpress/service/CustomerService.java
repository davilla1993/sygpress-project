package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.Customer;
import com.follysitou.sygpress.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public Customer create(Customer customer) {
        if (customerRepository.findByPhoneNumber(customer.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("Client", "téléphone", customer.getPhoneNumber());
        }
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer findByPublicId(String publicId) {
        return customerRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "publicId", publicId));
    }

    @Transactional(readOnly = true)
    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAllList() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Customer> searchByName(String name, Pageable pageable) {
        return customerRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional
    public Customer update(String publicId, Customer customerDetails) {
        Customer customer = findByPublicId(publicId);

        // Vérifier si le nouveau numéro de téléphone n'est pas déjà utilisé par un autre client
        customerRepository.findByPhoneNumber(customerDetails.getPhoneNumber())
                .ifPresent(existingCustomer -> {
                    if (!existingCustomer.getPublicId().equals(publicId)) {
                        throw new DuplicateResourceException("Client", "téléphone", customerDetails.getPhoneNumber());
                    }
                });

        customer.setName(customerDetails.getName());
        customer.setPhoneNumber(customerDetails.getPhoneNumber());
        customer.setAddress(customerDetails.getAddress());

        return customerRepository.save(customer);
    }

    @Transactional
    public void delete(String publicId) {
        Customer customer = findByPublicId(publicId);
        customerRepository.delete(customer);
    }
}
