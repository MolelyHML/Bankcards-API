package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CreateCardDTO;
import com.example.bankcards.entity.CardStatus;

import java.util.List;
import java.util.UUID;

public interface CardService {
    CardDTO createCard(CreateCardDTO createDTO);

    void deleteCard(UUID cardId);

    List<CardDTO> getAllCards(Integer page, Integer size);

    CardDTO changeCardStatus(UUID cardId, CardStatus status);
}
