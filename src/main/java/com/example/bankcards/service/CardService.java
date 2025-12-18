package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CreateCardDTO;
import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CardService {
    CardDTO createCard(CreateCardDTO createDTO);

    void deleteCard(UUID cardId);

    CardDTO changeCardStatus(UUID cardId, CardStatus status);

    List<CardDTO> getCards(Integer page, Integer size);

    CardDTO blockCard(UUID cardId);

    BigDecimal getCardBalance(UUID cardId);

    void transferMoney(UUID fromCardId, UUID toCardId, BigDecimal amount);
}
