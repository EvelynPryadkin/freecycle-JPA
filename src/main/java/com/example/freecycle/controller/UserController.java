package com.example.freecycle.controller;

import com.example.freecycle.dto.CreateUserRequest;
import com.example.freecycle.entity.Item;
import com.example.freecycle.entity.ItemInterest;
import com.example.freecycle.entity.User;
import com.example.freecycle.exception.NotFoundException;
import com.example.freecycle.repository.ItemInterestRepository;
import com.example.freecycle.repository.ItemRepository;
import com.example.freecycle.repository.UserRepository;
import com.example.freecycle.service.FreecycleService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final FreecycleService service;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final ItemInterestRepository interestRepo;

    public UserController(
            FreecycleService service,
            UserRepository userRepo,
            ItemRepository itemRepo,
            ItemInterestRepository interestRepo
    ) {
        this.service = service;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.interestRepo = interestRepo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody CreateUserRequest request) {
        return service.createUser(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@RequestBody CreateUserRequest request) {
        return service.createUser(request);
    }

    @GetMapping
    public List<User> findAll() {
        return userRepo.findAll();
    }

    @GetMapping("/{userId}")
    public User findById(@PathVariable Long userId) {
        return userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));
    }

    @GetMapping("/{userId}/items")
    public List<Item> findItemsByUser(@PathVariable Long userId) {
        return itemRepo.findByDonorId(userId);
    }

    @GetMapping("/{userId}/interests")
    public List<ItemInterest> findInterestsByUser(@PathVariable Long userId) {
        return interestRepo.findByUserId(userId);
    }
}
