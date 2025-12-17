package com.example.bankcards.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
