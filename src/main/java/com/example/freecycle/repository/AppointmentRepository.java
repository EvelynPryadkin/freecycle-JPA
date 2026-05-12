package com.example.freecycle.repository;

import com.example.freecycle.entity.Appointment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByItemId(Long itemId);
    List<Appointment> findByDonorIdOrRecipientId(Long donorId, Long recipientId);
    void deleteByItemId(Long itemId);
}
