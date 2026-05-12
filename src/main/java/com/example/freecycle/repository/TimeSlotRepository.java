package com.example.freecycle.repository;

import com.example.freecycle.entity.TimeSlot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByTransferSiteId(Long transferSiteId);
}
