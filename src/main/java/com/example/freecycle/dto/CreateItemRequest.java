package com.example.freecycle.dto;

public record CreateItemRequest(
        Long donorId,
        String title,
        String description,
        String category,
        String condition,
        String size,
        Integer quantity
) {
}
