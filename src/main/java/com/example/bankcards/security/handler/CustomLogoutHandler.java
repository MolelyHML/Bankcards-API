package com.example.bankcards.security.handler;

import com.example.bankcards.config.properties.JwtProperties;
import com.example.bankcards.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenRepository repository;
    private final JwtProperties properties;

    public CustomLogoutHandler(TokenRepository repository,
                               JwtProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       @Nullable Authentication authentication) {
        final var authHeader = request.getHeader(properties.getHeader());

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        final var token = authHeader.substring(7);
        final var tokenEntity = repository.findByAccessToken(token).orElse(null);

        if (tokenEntity != null) {
            tokenEntity.setLoggedOut(true);
            repository.save(tokenEntity);
        }
    }
}
