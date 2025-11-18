package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.Article;
import com.follysitou.sygpress.model.Pricing;
import com.follysitou.sygpress.repository.ArticleRepository;
import com.follysitou.sygpress.repository.PricingRepository;
import com.follysitou.sygpress.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRepository pricingRepository;
    private final ArticleRepository articleRepository;
    private final ServiceRepository serviceRepository;

    @Transactional
    public Pricing create(BigDecimal price, Long articleId, Long serviceId) {
        if (pricingRepository.existsByArticleIdAndServiceId(articleId, serviceId)) {
            throw new DuplicateResourceException("Un tarif existe déjà pour cet article et ce service");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        com.follysitou.sygpress.model.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));

        Pricing pricing = new Pricing();
        pricing.setPrice(price);
        pricing.setArticle(article);
        pricing.setService(service);

        return pricingRepository.save(pricing);
    }

    @Transactional(readOnly = true)
    public Pricing findById(Long id) {
        return pricingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarif", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Pricing> findAll() {
        return pricingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Pricing> findByArticle(Long articleId) {
        return pricingRepository.findByArticleId(articleId);
    }

    @Transactional(readOnly = true)
    public List<Pricing> findByService(Long serviceId) {
        return pricingRepository.findByServiceId(serviceId);
    }

    @Transactional(readOnly = true)
    public Pricing findByArticleAndService(Long articleId, Long serviceId) {
        return pricingRepository.findByArticleIdAndServiceId(articleId, serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tarif non trouvé pour l'article " + articleId + " et le service " + serviceId));
    }

    @Transactional
    public Pricing update(Long id, BigDecimal price, Long articleId, Long serviceId) {
        Pricing pricing = findById(id);

        // Vérifier si la nouvelle combinaison article/service n'existe pas déjà pour un autre tarif
        pricingRepository.findByArticleIdAndServiceId(articleId, serviceId)
                .ifPresent(existingPricing -> {
                    if (!existingPricing.getId().equals(id)) {
                        throw new DuplicateResourceException("Un tarif existe déjà pour cet article et ce service");
                    }
                });

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        com.follysitou.sygpress.model.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));

        pricing.setPrice(price);
        pricing.setArticle(article);
        pricing.setService(service);

        return pricingRepository.save(pricing);
    }

    @Transactional
    public void delete(Long id) {
        Pricing pricing = findById(id);
        pricingRepository.delete(pricing);
    }
}
