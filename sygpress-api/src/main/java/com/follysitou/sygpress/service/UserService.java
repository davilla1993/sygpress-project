package com.follysitou.sygpress.service;

import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.User;
import com.follysitou.sygpress.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public User findByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "publicId", publicId));
    }

    @Transactional
    public User toggleStatus(String publicId) {
        User user = findByPublicId(publicId);
        user.setEnabled(!user.getEnabled());
        return userRepository.save(user);
    }

    @Transactional
    public void delete(String publicId) {
        User user = findByPublicId(publicId);
        user.setDeleted(true);
        userRepository.save(user);
    }
}
