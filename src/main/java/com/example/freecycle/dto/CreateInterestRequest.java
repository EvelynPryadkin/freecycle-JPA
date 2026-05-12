package com.example.freecycle.dto;

public record CreateInterestRequest(
        Long userId,
        String message
) {
}
