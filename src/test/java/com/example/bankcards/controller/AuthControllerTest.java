package com.example.bankcards.controller;

import com.example.bankcards.BaseControllerTest;
import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.bankcards.security.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void authenticate_Success() throws Exception {
        AuthRequest request = new AuthRequest("user", "password");
        AuthResponse response = new AuthResponse("access_v1", "refresh_v1");

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_v1"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_v1"));
    }

    @Test
    void authenticate_InvalidCredentials() throws Exception {
        AuthRequest request = new AuthRequest("user", "wrong_pass");

        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid password"));

        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Проверьте данные запроса"));
    }

    @Test
    void refreshToken_Success() throws Exception {
        AuthResponse response = new AuthResponse("new_access", "new_refresh");

        when(authService.refreshToken(any())).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/auth/refresh-token")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid_refresh_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new_access"))
                .andExpect(jsonPath("$.refreshToken").value("new_refresh"));
    }

    @Test
    void refreshToken_InvalidToken() throws Exception {
        when(authService.refreshToken(any()))
                .thenReturn(ResponseEntity.status(401).build());

        mockMvc.perform(post("/auth/refresh-token")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_MissingHeader() throws Exception {
        when(authService.refreshToken(any()))
                .thenReturn(ResponseEntity.status(401).build());

        mockMvc.perform(post("/auth/refresh-token"))
                .andExpect(status().isUnauthorized());
    }
}