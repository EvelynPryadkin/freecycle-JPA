package com.example.freecycle.controller;

import com.example.freecycle.dto.CreateItemRequest;
import com.example.freecycle.entity.Item;
import com.example.freecycle.entity.ItemState;
import com.example.freecycle.exception.NotFoundException;
import com.example.freecycle.repository.ItemRepository;
import com.example.freecycle.service.FreecycleService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository itemRepo;
    private final FreecycleService service;

    public ItemController(ItemRepository itemRepo, FreecycleService service) {
        this.itemRepo = itemRepo;
        this.service = service;
    }

    @GetMapping
    public List<Item> findAll(@RequestParam(required = false) ItemState state) {
        return state == null ? itemRepo.findAll() : itemRepo.findByState(state);
    }

    @GetMapping("/{itemId}")
    public Item findById(@PathVariable Long itemId) {
        return itemRepo.findById(itemId).orElseThrow(() -> new NotFoundException("Item not found."));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Item create(@RequestBody CreateItemRequest request) {
        return service.createItem(request);
    }
}
