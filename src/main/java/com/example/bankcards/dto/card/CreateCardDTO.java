package com.example.bankcards.dto.card;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateCardDTO(
        @NotBlank(message = "Номер карты не может быть пустым")
        @Length(min = 16, max = 19, message = "Номер карты должен содержать от 16 до 19 цифр")
        String pan,

        @NotNull(message = "Начальный баланс должен быть указан")
        @DecimalMin(value = "0.0", message = "Баланс не может быть отрицательным")
        @Digits(integer = 15, fraction = 2, message = "Баланс должен иметь формат (15 знаков и 2 после запятой)")
        BigDecimal balance,

        @NotNull(message = "ID владельца обязателен")
        UUID ownerId,

        @NotNull(message = "Дата истечения обязательна")
        @Future(message = "Дата истечения карты должна быть в будущем")
        LocalDate expiredAt
) {
}
