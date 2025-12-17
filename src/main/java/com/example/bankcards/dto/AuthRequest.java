package com.example.bankcards.dto;

public record AuthRequest(
        String username,
        String password
) {}
