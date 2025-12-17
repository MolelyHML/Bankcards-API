package com.example.bankcards.service;

import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;
import com.example.bankcards.dto.auth.RegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.AuthService;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("testuser", "Full Name", "password");
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");

        authService.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ThrowsException_IfUserExists() {
        RegisterRequest request = new RegisterRequest("testuser", "Full Name", "password");
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_Success() {
        AuthRequest request = new AuthRequest("testuser", "password");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn("access");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh");

        AuthResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("access", response.accessToken());
        verify(tokenService).saveAndRevokeUserToken(any(), eq("access"), eq("refresh"), any());
    }

    @Test
    void refreshToken_Unauthorized_WhenHeaderMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        var response = authService.refreshToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
