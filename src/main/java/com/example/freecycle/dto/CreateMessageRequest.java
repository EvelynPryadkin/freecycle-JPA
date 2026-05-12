package com.example.freecycle.dto;

public record CreateMessageRequest(
        Long senderId,
        Long recipientId,
        Long itemId,
        String subject,
        String content
) {
}
