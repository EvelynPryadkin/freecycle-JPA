package com.example.freecycle.controller;

import com.example.freecycle.entity.Appointment;
import com.example.freecycle.exception.NotFoundException;
import com.example.freecycle.repository.AppointmentRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepository appointmentRepo;

    public AppointmentController(AppointmentRepository appointmentRepo) {
        this.appointmentRepo = appointmentRepo;
    }

    @GetMapping
    public List<Appointment> findAll() {
        return appointmentRepo.findAll();
    }

    @GetMapping("/{appointmentId}")
    public Appointment findById(@PathVariable Long appointmentId) {
        return appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found."));
    }

    @GetMapping("/item/{itemId}")
    public Appointment findByItem(@PathVariable Long itemId) {
        return appointmentRepo.findByItemId(itemId)
                .orElseThrow(() -> new NotFoundException("Appointment not found."));
    }

    @GetMapping("/users/{userId}")
    public List<Appointment> findByUser(@PathVariable Long userId) {
        return appointmentRepo.findByDonorIdOrRecipientId(userId, userId);
    }
}
