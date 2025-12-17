package com.example.bankcards.controller;

import com.example.bankcards.BaseControllerTest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.security.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegisterControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_ShouldReturnOk() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "Full Name", "password");
        doNothing().when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenJsonIsInvalid() throws Exception {
        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturnError_WhenUserAlreadyExists() throws Exception {
        doThrow(new RuntimeException("Пользователь с такими именем уже существует"))
                .when(authService).register(any(RegisterRequest.class));

        RegisterRequest request = new RegisterRequest("existing_user", "Name", "pass");

        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Пользователь с такими именем уже существует"));
    }
}
