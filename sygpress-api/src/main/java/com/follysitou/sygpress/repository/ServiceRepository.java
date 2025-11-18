package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.LaundryService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<LaundryService, Long> {
    Optional<LaundryService> findByPublicId(String publicId);
    Optional<LaundryService> findByName(String name);
    boolean existsByName(String name);
}
