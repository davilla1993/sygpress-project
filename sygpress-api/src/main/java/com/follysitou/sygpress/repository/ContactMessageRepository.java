package com.follysitou.sygpress.repository;

import com.follysitou.sygpress.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findByReadStatusOrderByCreatedAtDesc(Boolean readStatus);

    List<ContactMessage> findAllByOrderByCreatedAtDesc();
}
