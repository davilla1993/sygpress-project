package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.Article;
import com.follysitou.sygpress.model.Pricing;
import com.follysitou.sygpress.repository.ArticleRepository;
import com.follysitou.sygpress.repository.PricingRepository;
import com.follysitou.sygpress.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRepository pricingRepository;
    private final ArticleRepository articleRepository;
    private final ServiceRepository serviceRepository;

    @Transactional
    public Pricing create(BigDecimal price, String articlePublicId, String servicePublicId) {
        if (pricingRepository.existsByArticlePublicIdAndServicePublicId(articlePublicId, servicePublicId)) {
            throw new DuplicateResourceException("Un tarif existe déjà pour cet article et ce service");
        }

        Article article = articleRepository.findByPublicId(articlePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "publicId", articlePublicId));

        com.follysitou.sygpress.model.Service service = serviceRepository.findByPublicId(servicePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "publicId", servicePublicId));

        Pricing pricing = new Pricing();
        pricing.setPrice(price);
        pricing.setArticle(article);
        pricing.setService(service);

        return pricingRepository.save(pricing);
    }

    @Transactional(readOnly = true)
    public Pricing findByPublicId(String publicId) {
        return pricingRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarif", "publicId", publicId));
    }

    @Transactional(readOnly = true)
    public Page<Pricing> findAll(Pageable pageable) {
        return pricingRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Pricing> findByArticle(String articlePublicId, Pageable pageable) {
        return pricingRepository.findByArticlePublicId(articlePublicId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Pricing> findByService(String servicePublicId, Pageable pageable) {
        return pricingRepository.findByServicePublicId(servicePublicId, pageable);
    }

    @Transactional(readOnly = true)
    public Pricing findByArticleAndService(String articlePublicId, String servicePublicId) {
        return pricingRepository.findByArticlePublicIdAndServicePublicId(articlePublicId, servicePublicId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tarif non trouvé pour l'article " + articlePublicId + " et le service " + servicePublicId));
    }

    @Transactional
    public Pricing update(String publicId, BigDecimal price, String articlePublicId, String servicePublicId) {
        Pricing pricing = findByPublicId(publicId);

        // Vérifier si la nouvelle combinaison article/service n'existe pas déjà pour un autre tarif
        pricingRepository.findByArticlePublicIdAndServicePublicId(articlePublicId, servicePublicId)
                .ifPresent(existingPricing -> {
                    if (!existingPricing.getPublicId().equals(publicId)) {
                        throw new DuplicateResourceException("Un tarif existe déjà pour cet article et ce service");
                    }
                });

        Article article = articleRepository.findByPublicId(articlePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "publicId", articlePublicId));

        com.follysitou.sygpress.model.Service service = serviceRepository.findByPublicId(servicePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "publicId", servicePublicId));

        pricing.setPrice(price);
        pricing.setArticle(article);
        pricing.setService(service);

        return pricingRepository.save(pricing);
    }

    @Transactional
    public void delete(String publicId) {
        Pricing pricing = findByPublicId(publicId);
        pricingRepository.delete(pricing);
    }
}
