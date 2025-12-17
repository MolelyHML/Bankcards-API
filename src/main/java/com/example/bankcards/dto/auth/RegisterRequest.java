package com.example.bankcards.dto.auth;

public record RegisterRequest(
        String username,
        String fullName,
        String password
) {
}
