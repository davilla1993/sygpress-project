package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByPublicId(String publicId);
    Optional<Company> findFirstByDeletedFalse();
}
