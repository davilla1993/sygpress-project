package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    Optional<Service> findByPublicId(String publicId);
    Optional<Service> findByName(String name);
    boolean existsByName(String name);
}
