package com.example.bankcards.security;

import com.example.bankcards.entity.User;

import java.util.UUID;

public interface TokenService {
    void saveAndRevokeUserToken(UUID userId, String accessToken, String refreshToken, User user);
}
