package com.example.bankcards.dto.auth;

public record AuthRequest(
        String username,
        String password
) {}
