package com.example.freecycle.controller;

import com.example.freecycle.dto.CreateMessageRequest;
import com.example.freecycle.entity.Message;
import com.example.freecycle.exception.NotFoundException;
import com.example.freecycle.repository.MessageRepository;
import com.example.freecycle.service.FreecycleService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepo;
    private final FreecycleService service;

    public MessageController(MessageRepository messageRepo, FreecycleService service) {
        this.messageRepo = messageRepo;
        this.service = service;
    }

    @GetMapping
    public List<Message> findAll() {
        return messageRepo.findAll();
    }

    @GetMapping("/{messageId}")
    public Message findById(@PathVariable Long messageId) {
        return messageRepo.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found."));
    }

    @GetMapping("/recipient/{recipientId}")
    public List<Message> findByRecipient(@PathVariable Long recipientId) {
        return messageRepo.findByRecipientId(recipientId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Message create(@RequestBody CreateMessageRequest request) {
        return service.createMessage(request);
    }

    @PostMapping("/{messageId}/read")
    public Message markRead(@PathVariable Long messageId) {
        return service.markMessageRead(messageId);
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long messageId) {
        messageRepo.deleteById(messageId);
    }
}
