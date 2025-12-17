package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CreateCardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CardServiceImpl cardService;

    private User testOwner;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        testOwner = new User();
        testOwner.setId(ownerId);
    }

    @Test
    void createCard_Success() {
        CreateCardDTO dto = new CreateCardDTO("1234567812345678", BigDecimal.TEN, ownerId, LocalDate.now().plusYears(1));

        when(cardRepository.existsByPan(dto.pan())).thenReturn(false);
        when(userService.getById(ownerId)).thenReturn(testOwner);

        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card savedCard = invocation.getArgument(0);
            savedCard.setId(UUID.randomUUID());
            return savedCard;
        });

        CardDTO result = cardService.createCard(dto);

        assertNotNull(result);
        assertEquals(CardStatus.ACTIVE, result.status());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_DuplicatePan_ThrowsException() {
        CreateCardDTO dto = new CreateCardDTO("1234567812345678", BigDecimal.TEN, ownerId, LocalDate.now().plusYears(1));
        when(cardRepository.existsByPan(dto.pan())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cardService.createCard(dto));
        assertEquals("Карта с таким номером уже зарегистрирована", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void changeCardStatus_Success() {
        UUID cardId = UUID.randomUUID();
        Card existingCard = new Card();
        existingCard.setId(cardId);
        existingCard.setPan("1234567812345678");
        existingCard.setStatus(CardStatus.ACTIVE);
        existingCard.setOwner(testOwner);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));

        CardDTO result = cardService.changeCardStatus(cardId, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, result.status());
        assertEquals(CardStatus.BLOCKED, existingCard.getStatus()); // Проверка dirty checking/обновления объекта
    }

    @Test
    void getAllCards_ReturnsList() {
        PageRequest pageable = PageRequest.of(0, 5);
        Card card = new Card();
        card.setPan("1111222233334444");
        card.setOwner(testOwner);

        when(cardRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(card)));

        List<CardDTO> result = cardService.getAllCards(0, 5);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository).findAll(pageable);
    }

    @Test
    void deleteCard_CallsRepository() {
        UUID cardId = UUID.randomUUID();

        cardService.deleteCard(cardId);

        verify(cardRepository, times(1)).deleteById(cardId);
    }
}