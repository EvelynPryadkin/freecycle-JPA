package com.example.freecycle.dto;

import java.time.LocalDateTime;

public record CreateTimeSlotRequest(
        Long transferSiteId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer maxCapacity
) {
}
