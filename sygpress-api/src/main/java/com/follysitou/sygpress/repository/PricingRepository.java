package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.Pricing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Long> {
    Optional<Pricing> findByPublicId(String publicId);
    Optional<Pricing> findByArticleIdAndServiceId(Long articleId, Long serviceId);
    Optional<Pricing> findByArticlePublicIdAndServicePublicId(String articlePublicId, String servicePublicId);
    List<Pricing> findByArticleId(Long articleId);
    List<Pricing> findByServiceId(Long serviceId);
    Page<Pricing> findByArticlePublicId(String articlePublicId, Pageable pageable);
    Page<Pricing> findByServicePublicId(String servicePublicId, Pageable pageable);
    boolean existsByArticleIdAndServiceId(Long articleId, Long serviceId);
    boolean existsByArticlePublicIdAndServicePublicId(String articlePublicId, String servicePublicId);
}
