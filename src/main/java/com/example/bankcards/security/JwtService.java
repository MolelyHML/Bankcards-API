package com.example.bankcards.security;

import com.example.bankcards.config.properties.JwtProperties;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

    private final JwtProperties properties;
    private final TokenRepository tokenRepository;

    public JwtService(JwtProperties properties, TokenRepository tokenRepository) {
        this.properties = properties;
        this.tokenRepository = tokenRepository;
    }

    public boolean isValid(String token, UserDetails user) {
        final var username = extractUsername(token);

        boolean isValidToken = tokenRepository.findByAccessToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);

        return username.equals(user.getUsername())
                && isAccessTokenExpired(token)
                && isValidToken;
    }

    public boolean isValidRefresh(String token, User user) {
        final var username = extractUsername(token);

        boolean isValidRefreshToken = tokenRepository.findByRefreshToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);

        return username.equals(user.getUsername())
                && isAccessTokenExpired(token)
                && isValidRefreshToken;
    }

    private boolean isAccessTokenExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final var claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails user) {
        return generateToken(user, properties.getAccessTokenExpiration());
    }

    public String generateRefreshToken(UserDetails user) {
        return generateToken(user, properties.getRefreshTokenExpiration());
    }

    private String generateToken(UserDetails user, long expiryTime) {
        final var builder = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiryTime))
                .claim("role", user.getAuthorities().iterator().next().getAuthority())
                .signWith(getSignInKey());

        return builder.compact();
    }

    private Claims extractAllClaims(String token) {
        final var parser = Jwts.parser();

        parser.verifyWith(getSignInKey());

        return parser.build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
