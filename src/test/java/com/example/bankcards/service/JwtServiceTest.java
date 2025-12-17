package com.example.bankcards.service;

import com.example.bankcards.config.properties.JwtProperties;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.TokenRepository;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    @Mock private TokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        String secret = Base64.getEncoder().encodeToString("супер-секретный-ключ-длинною-64-байта-для-hmac-sha-512-алгоритма".getBytes());
        properties.setSecret(secret);
        properties.setAccessTokenExpiration(3600000);
        properties.setRefreshTokenExpiration(86400000);

        jwtService = new JwtService(properties, tokenRepository);
    }

    @Test
    void generateAndExtractUsername_Success() {
        User user = new User();
        user.setUsername("stanislav");
        user.setRole(User.Role.ROLE_USER);

        String token = jwtService.generateAccessToken(user);
        String extractedUsername = jwtService.extractUsername(token);

        assertEquals("stanislav", extractedUsername);
    }
}
