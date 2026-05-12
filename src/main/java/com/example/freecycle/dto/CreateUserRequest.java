package com.example.freecycle.dto;

public record CreateUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber,
        String address
) {
}
