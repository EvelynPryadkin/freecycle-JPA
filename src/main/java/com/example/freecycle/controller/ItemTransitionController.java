package com.example.freecycle.controller;

import com.example.freecycle.dto.ScheduleRequest;
import com.example.freecycle.entity.Appointment;
import com.example.freecycle.entity.Item;
import com.example.freecycle.service.FreecycleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class ItemTransitionController {

    private final FreecycleService service;

    public ItemTransitionController(FreecycleService service) {
        this.service = service;
    }

    @PostMapping("/{itemId}/select/{interestId}")
    public Item selectRecipient(@PathVariable Long itemId, @PathVariable Long interestId) {
        return service.selectRecipient(itemId, interestId);
    }

    @PostMapping("/{itemId}/deselect")
    public Item deselectRecipient(@PathVariable Long itemId) {
        return service.deselectRecipient(itemId);
    }

    @PostMapping("/{itemId}/schedule")
    public Appointment schedule(@PathVariable Long itemId, @RequestBody ScheduleRequest request) {
        return service.schedule(itemId, request);
    }

    @PostMapping("/{itemId}/complete")
    public Item complete(@PathVariable Long itemId) {
        return service.complete(itemId);
    }

    @PostMapping("/{itemId}/deschedule")
    public Item deschedule(@PathVariable Long itemId) {
        return service.deschedule(itemId);
    }

    @PostMapping("/{itemId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOffer(@PathVariable Long itemId) {
        service.cancelOffer(itemId);
    }
}
