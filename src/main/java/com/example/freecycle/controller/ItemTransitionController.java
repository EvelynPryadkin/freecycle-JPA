package com.example.freecycle.controller;

import com.example.freecycle.dto.ScheduleRequest;
import com.example.freecycle.entity.Appointment;
import com.example.freecycle.entity.Item;
import com.example.freecycle.security.FreecycleUserDetails;
import com.example.freecycle.service.FreecycleService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    public Item selectRecipient(Authentication authentication, @PathVariable Long itemId, @PathVariable Long interestId) {
        return service.selectRecipient(itemId, authenticatedUserId(authentication), interestId);
    }

    @PostMapping("/{itemId}/deselect")
    public Item deselectRecipient(Authentication authentication, @PathVariable Long itemId) {
        return service.deselectRecipient(itemId, authenticatedUserId(authentication));
    }

    @PostMapping("/{itemId}/schedule")
    public Appointment schedule(Authentication authentication, @PathVariable Long itemId, @RequestBody ScheduleRequest request) {
        return service.schedule(itemId, authenticatedUserId(authentication), request);
    }

    @PostMapping("/{itemId}/complete")
    public Item complete(Authentication authentication, @PathVariable Long itemId) {
        return service.complete(itemId, authenticatedUserId(authentication));
    }

    @PostMapping("/{itemId}/deschedule")
    public Item deschedule(Authentication authentication, @PathVariable Long itemId) {
        return service.deschedule(itemId, authenticatedUserId(authentication));
    }

    @PostMapping("/{itemId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOffer(Authentication authentication, @PathVariable Long itemId) {
        service.cancelOffer(itemId, authenticatedUserId(authentication));
    }

    private Long authenticatedUserId(Authentication authentication) {
        FreecycleUserDetails userDetails = (FreecycleUserDetails) authentication.getPrincipal();
        return Long.parseLong(userDetails.getUsername());
    }
}
