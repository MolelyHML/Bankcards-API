package com.example.bankcards.security.impl;

import com.example.bankcards.entity.Token;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.TokenRepository;
import com.example.bankcards.security.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {

    private final TokenRepository repository;

    public TokenServiceImpl(TokenRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void saveAndRevokeUserToken(UUID userId, String accessToken, String refreshToken, User user) {
        repository.revokeAllTokensByUserId(userId);
        saveUserToken(accessToken, refreshToken, user);
    }

    private void saveUserToken(String accessToken, String refreshToken, User user) {
        final var token = new Token();

        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);

        repository.save(token);
    }
}
