package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.Pricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Long> {
    Optional<Pricing> findByArticleIdAndServiceId(Long articleId, Long serviceId);
    List<Pricing> findByArticleId(Long articleId);
    List<Pricing> findByServiceId(Long serviceId);
    boolean existsByArticleIdAndServiceId(Long articleId, Long serviceId);
}
