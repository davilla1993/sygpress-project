package com.follysitou.sygpress.controller;

import com.follysitou.sygpress.dto.ContactMessageDTO;
import com.follysitou.sygpress.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "API pour la gestion des messages de contact")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    @Operation(summary = "Envoyer un message de contact", description = "Endpoint public pour envoyer un message de contact")
    public ResponseEntity<ContactMessageDTO> sendMessage(@Valid @RequestBody ContactMessageDTO dto) {
        ContactMessageDTO createdMessage = contactMessageService.createMessage(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer tous les messages", description = "Récupère tous les messages de contact (admin uniquement)")
    public ResponseEntity<List<ContactMessageDTO>> getAllMessages() {
        List<ContactMessageDTO> messages = contactMessageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer les messages non lus", description = "Récupère tous les messages non lus (admin uniquement)")
    public ResponseEntity<List<ContactMessageDTO>> getUnreadMessages() {
        List<ContactMessageDTO> messages = contactMessageService.getUnreadMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer un message par ID", description = "Récupère un message de contact spécifique (admin uniquement)")
    public ResponseEntity<ContactMessageDTO> getMessageById(@PathVariable Long id) {
        ContactMessageDTO message = contactMessageService.getMessageById(id);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Marquer un message comme lu", description = "Marque un message de contact comme lu (admin uniquement)")
    public ResponseEntity<ContactMessageDTO> markAsRead(@PathVariable Long id) {
        ContactMessageDTO message = contactMessageService.markAsRead(id);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un message", description = "Supprime un message de contact (admin uniquement)")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        contactMessageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
