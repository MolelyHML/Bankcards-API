package com.example.bankcards.controller;

import com.example.bankcards.BaseControllerTest;
import com.example.bankcards.dto.user.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Admin_Success() throws Exception {
        UserDTO userDTO = new UserDTO(UUID.randomUUID(), "admin_user", "ADMIN");
        when(userService.getAllUsers(any(), any())).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin_user"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_User_Forbidden() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserById_SpecificId_Success() throws Exception {
        UUID targetId = UUID.randomUUID();
        UserDTO expectedUser = new UserDTO(targetId, "some_user", "USER");

        when(userService.getUserByIdOrMe(eq(targetId))).thenReturn(expectedUser);

        mockMvc.perform(get("/users/get")
                        .param("id", targetId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetId.toString()))
                .andExpect(jsonPath("$.username").value("some_user"));
    }

    @Test
    @WithMockUser(username = "current_user", roles = "USER")
    void getUserById_Me_Success() throws Exception {
        UserDTO currentUser = new UserDTO(UUID.randomUUID(), "current_user", "USER");

        when(userService.getUserByIdOrMe(null)).thenReturn(currentUser);

        mockMvc.perform(get("/users/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("current_user"));
    }

    @Test
    void getAllUsers_Anonymous_Unauthorized() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }
}