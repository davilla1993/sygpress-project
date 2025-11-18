package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.Customer;
import com.follysitou.sygpress.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public Customer create(Customer customer) {
        if (customerRepository.findByPhone(customer.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("Client", "téléphone", customer.getPhoneNumber());
        }
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Customer> searchByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Customer update(Long id, Customer customerDetails) {
        Customer customer = findById(id);

        // Vérifier si le nouveau numéro de téléphone n'est pas déjà utilisé par un autre client
        customerRepository.findByPhone(customerDetails.getPhoneNumber())
                .ifPresent(existingCustomer -> {
                    if (!existingCustomer.getId().equals(id)) {
                        throw new DuplicateResourceException("Client", "téléphone", customerDetails.getPhoneNumber());
                    }
                });

        customer.setName(customerDetails.getName());
        customer.setPhoneNumber(customerDetails.getPhoneNumber());
        customer.setAddress(customerDetails.getAddress());

        return customerRepository.save(customer);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findById(id);
        customerRepository.delete(customer);
    }
}
