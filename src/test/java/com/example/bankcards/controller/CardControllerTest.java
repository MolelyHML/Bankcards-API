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
    void getCards_NoParams_ReturnsOkWithDefaults() throws Exception {
        mockMvc.perform(get("/cards"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCards_User_Success() throws Exception {
        mockMvc.perform(get("/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void blockCard_Success() throws Exception {
        UUID cardId = UUID.randomUUID();

        when(cardService.blockCard(cardId)).thenReturn(new CardDTO(
                cardId, "************1234", CardStatus.BLOCKED, BigDecimal.ZERO, LocalDate.now(), UUID.randomUUID()
        ));

        mockMvc.perform(patch("/cards/{cardId}/block", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBalance_Success() throws Exception {
        UUID cardId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("1500.50");

        when(cardService.getCardBalance(cardId)).thenReturn(balance);

        mockMvc.perform(get("/cards/{cardId}/balance", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void transfer_Success() throws Exception {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("500.00");

        mockMvc.perform(post("/cards/transfer")
                        .param("fromCardId", fromId.toString())
                        .param("toCardId", toId.toString())
                        .param("amount", amount.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_Admin_Forbidden() throws Exception {
        mockMvc.perform(patch("/cards/{cardId}/block", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void transfer_Admin_Forbidden() throws Exception {
        mockMvc.perform(post("/cards/transfer")
                        .param("fromCardId", UUID.randomUUID().toString())
                        .param("toCardId", UUID.randomUUID().toString())
                        .param("amount", "100"))
                .andExpect(status().isForbidden());
    }
}
