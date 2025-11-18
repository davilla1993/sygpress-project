package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.DuplicateResourceException;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public com.follysitou.sygpress.model.Service findById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
    }

    @Transactional(readOnly = true)
    public List<com.follysitou.sygpress.model.Service> findAll() {
        return serviceRepository.findAll();
    }

    @Transactional
    public com.follysitou.sygpress.model.Service update(Long id, com.follysitou.sygpress.model.Service serviceDetails) {
        com.follysitou.sygpress.model.Service service = findById(id);

        // Vérifier si le nouveau nom n'est pas déjà utilisé par un autre service
        serviceRepository.findByName(serviceDetails.getName())
                .ifPresent(existingService -> {
                    if (!existingService.getId().equals(id)) {
                        throw new DuplicateResourceException("Service", "nom", serviceDetails.getName());
                    }
                });

        service.setName(serviceDetails.getName());

        return serviceRepository.save(service);
    }

    @Transactional
    public void delete(Long id) {
        com.follysitou.sygpress.model.Service service = findById(id);
        serviceRepository.delete(service);
    }
}
