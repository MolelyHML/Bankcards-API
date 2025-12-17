package com.example.bankcards.dto.card;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CardDTO(
        UUID id,
        String maskedPan,
        CardStatus status,
        BigDecimal balance,
        LocalDate expiredAt,
        UUID ownerId)
{
    public CardDTO(Card card) {
        this(
                card.getId(), maskPan(card.getPan()),
                card.getStatus(), card.getBalance(),
                card.getExpiredAt(), card.getOwner().getId()
        );
    }

    private static String maskPan(String pan) {
        return "*".repeat(pan.length() - 4) + pan.substring(pan.length() - 4);
    }
}
