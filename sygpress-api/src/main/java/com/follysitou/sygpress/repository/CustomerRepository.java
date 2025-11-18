package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPublicId(String publicId);
    Optional<Customer> findByPhoneNumber(String phone);
    List<Customer> findByNameContainingIgnoreCase(String name);
    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
