package com.example.bankcards;

import com.example.bankcards.config.properties.JwtProperties;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.handler.CustomAccessDeniedHandler;
import com.example.bankcards.security.handler.CustomLogoutHandler;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Import(BaseControllerTest.ObjectMapperTestConfig.class)
public abstract class BaseControllerTest {
    @MockitoBean protected JwtService jwtService;
    @MockitoBean protected UserService userService;
    @MockitoBean protected CardService cardService;
    @MockitoBean protected CustomAccessDeniedHandler accessDeniedHandler;
    @MockitoBean protected CustomLogoutHandler customLogoutHandler;
    @MockitoBean protected JwtProperties jwtProperties;

    public static class ObjectMapperTestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper;
        }
    }
}