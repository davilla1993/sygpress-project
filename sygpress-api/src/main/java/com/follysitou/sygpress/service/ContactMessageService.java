package com.follysitou.sygpress.service;

import com.follysitou.sygpress.dto.ContactMessageDTO;
import com.follysitou.sygpress.exception.ResourceNotFoundException;
import com.follysitou.sygpress.model.ContactMessage;
import com.follysitou.sygpress.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    @Transactional
    public ContactMessageDTO createMessage(ContactMessageDTO dto) {
        log.info("Creating new contact message from: {}", dto.getEmail());

        ContactMessage message = new ContactMessage();
        message.setName(dto.getName());
        message.setEmail(dto.getEmail());
        message.setPhone(dto.getPhone());
        message.setSubject(dto.getSubject());
        message.setMessage(dto.getMessage());

        ContactMessage savedMessage = contactMessageRepository.save(message);
        log.info("Contact message created with ID: {}", savedMessage.getId());

        return convertToDTO(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<ContactMessageDTO> getAllMessages() {
        log.info("Fetching all contact messages");
        return contactMessageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContactMessageDTO> getUnreadMessages() {
        log.info("Fetching unread contact messages");
        return contactMessageRepository.findByReadStatusOrderByCreatedAtDesc(false)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContactMessageDTO getMessageById(Long id) {
        log.info("Fetching contact message with ID: {}", id);
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
        return convertToDTO(message);
    }

    @Transactional
    public ContactMessageDTO markAsRead(Long id) {
        log.info("Marking contact message {} as read", id);
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
        message.setReadStatus(true);
        ContactMessage updatedMessage = contactMessageRepository.save(message);
        return convertToDTO(updatedMessage);
    }

    @Transactional
    public void deleteMessage(Long id) {
        log.info("Deleting contact message with ID: {}", id);
        if (!contactMessageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contact message not found with id: " + id);
        }
        contactMessageRepository.deleteById(id);
        log.info("Contact message {} deleted successfully", id);
    }

    private ContactMessageDTO convertToDTO(ContactMessage message) {
        ContactMessageDTO dto = new ContactMessageDTO();
        dto.setId(message.getId());
        dto.setName(message.getName());
        dto.setEmail(message.getEmail());
        dto.setPhone(message.getPhone());
        dto.setSubject(message.getSubject());
        dto.setMessage(message.getMessage());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setReadStatus(message.getReadStatus());
        return dto;
    }
}
