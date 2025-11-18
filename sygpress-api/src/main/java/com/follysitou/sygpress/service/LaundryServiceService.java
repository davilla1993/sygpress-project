package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class LaundryServiceService {

    private final ServiceRepository serviceRepository;

    @Transactional
    public com.follysitou.sygpress.model.Service create(com.follysitou.sygpress.model.Service service) {
        if (serviceRepository.existsByName(service.getName())) {
            throw new DuplicateResourceException("Service", "nom", service.getName());
        }
        return serviceRepository.save(service);
    }

    @Transactional(readOnly = true)
    public com.follysitou.sygpress.model.Service findByPublicId(String publicId) {
        return serviceRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "publicId", publicId));
    }

    @Transactional(readOnly = true)
    public Page<com.follysitou.sygpress.model.Service> findAll(Pageable pageable) {
        return serviceRepository.findAll(pageable);
    }

    @Transactional
    public com.follysitou.sygpress.model.Service update(String publicId, com.follysitou.sygpress.model.Service serviceDetails) {
        com.follysitou.sygpress.model.Service service = findByPublicId(publicId);

        // Vérifier si le nouveau nom n'est pas déjà utilisé par un autre service
        serviceRepository.findByName(serviceDetails.getName())
                .ifPresent(existingService -> {
                    if (!existingService.getPublicId().equals(publicId)) {
                        throw new DuplicateResourceException("Service", "nom", serviceDetails.getName());
                    }
                });

        service.setName(serviceDetails.getName());

        return serviceRepository.save(service);
    }

    @Transactional
    public void delete(String publicId) {
        com.follysitou.sygpress.model.Service service = findByPublicId(publicId);
        serviceRepository.delete(service);
    }
}
