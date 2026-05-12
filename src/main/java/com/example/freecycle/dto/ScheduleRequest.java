package com.example.freecycle.dto;

public record ScheduleRequest(
        Long timeSlotId,
        String notes
) {
}
