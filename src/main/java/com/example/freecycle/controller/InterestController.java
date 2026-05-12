package com.example.freecycle.controller;

import com.example.freecycle.dto.CreateInterestRequest;
import com.example.freecycle.entity.ItemInterest;
import com.example.freecycle.repository.ItemInterestRepository;
import com.example.freecycle.service.FreecycleService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items/{itemId}/interests")
public class InterestController {

    private final ItemInterestRepository interestRepo;
    private final FreecycleService service;

    public InterestController(ItemInterestRepository interestRepo, FreecycleService service) {
        this.interestRepo = interestRepo;
        this.service = service;
    }

    @GetMapping
    public List<ItemInterest> findByItem(@PathVariable Long itemId) {
        return interestRepo.findByItemId(itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemInterest create(@PathVariable Long itemId, @RequestBody CreateInterestRequest request) {
        return service.createInterest(itemId, request);
    }

    @DeleteMapping("/{interestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable Long itemId, @PathVariable Long interestId) {
        service.withdrawInterest(itemId, interestId);
    }
}
