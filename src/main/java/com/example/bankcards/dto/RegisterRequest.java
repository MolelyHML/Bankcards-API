package com.example.bankcards.dto;

public record RegisterRequest(
        String username,
        String fullName,
        String password
) {
}
