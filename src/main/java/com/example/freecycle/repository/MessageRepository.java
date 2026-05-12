package com.example.freecycle.repository;

import com.example.freecycle.entity.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRecipientId(Long recipientId);
}
