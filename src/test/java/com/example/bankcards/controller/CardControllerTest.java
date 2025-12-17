package com.example.bankcards.controller;

import com.example.bankcards.BaseControllerTest;
import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CreateCardDTO;
import com.example.bankcards.entity.CardStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
class CardControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**Включение обработки @PreAuthorize без импортирования
    всего конфига безопасности
     */
    @TestConfiguration
    @EnableMethodSecurity
    static class SecurityTestConfig {
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_Success() throws Exception {
        CreateCardDTO request = new CreateCardDTO(
                "1234567812345678",
                BigDecimal.valueOf(10.0),
                UUID.randomUUID(),
                LocalDate.now().plusYears(1)
        );

        when(cardService.createCard(any())).thenReturn(new CardDTO(
                UUID.randomUUID(), "****5678", CardStatus.ACTIVE, BigDecimal.TEN, LocalDate.now(), UUID.randomUUID()
        ));

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeStatus_Success() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/cards/change-status")
                        .param("cardId", id.toString())
                        .param("newStatus", "BLOCKED"))
                .andExpect(status().isOk());
    }

    private static Stream<Arguments> provideInvalidCardData() {
        return Stream.of(
                Arguments.of("123", BigDecimal.TEN, LocalDate.now().plusDays(1), "pan"),
                Arguments.of("1234567812345678", BigDecimal.valueOf(-1), LocalDate.now().plusDays(1), "balance"),
                Arguments.of("1234567812345678", BigDecimal.TEN, LocalDate.now().minusDays(1), "expiredAt"),
                Arguments.of("", BigDecimal.TEN, LocalDate.now().plusDays(1), "pan")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCardData")
    @WithMockUser(roles = "ADMIN")
    void createCard_ValidationFails(String pan, BigDecimal balance, LocalDate expiry, String errorField) throws Exception {
        CreateCardDTO request = new CreateCardDTO(pan, balance, UUID.randomUUID(), expiry);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + errorField).exists());
    }


    @Test
    @WithMockUser(roles = "USER")
    void accessDenied_ForUser() throws Exception {
        mockMvc.perform(delete("/cards/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_MissingParam() throws Exception {
        mockMvc.perform(get("/cards"))
                .andExpect(status().isBadRequest());
    }
}
