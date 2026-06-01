package com.example.freecycle.controller;

import com.example.freecycle.dto.CreateUserRequest;
import com.example.freecycle.dto.LoginRequest;
import com.example.freecycle.entity.Item;
import com.example.freecycle.entity.ItemInterest;
import com.example.freecycle.entity.User;
import com.example.freecycle.exception.NotFoundException;
import com.example.freecycle.repository.ItemInterestRepository;
import com.example.freecycle.repository.ItemRepository;
import com.example.freecycle.repository.UserRepository;
import com.example.freecycle.service.FreecycleService;
import com.example.freecycle.service.JwtService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            FreecycleService service,
            UserRepository userRepo,
            ItemRepository itemRepo,
            ItemInterestRepository interestRepo,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.service = service;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.interestRepo = interestRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
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

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> user = userRepo.findByEmail(loginRequest.getEmail());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        
        User foundUser = user.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), foundUser.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        
        String token = jwtService.makeJwt(foundUser.getId().toString());
        return ResponseEntity.ok().body(token);
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
