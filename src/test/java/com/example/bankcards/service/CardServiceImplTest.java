package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CreateCardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
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
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        testOwner = new User();
        testOwner.setId(ownerId);
        testOwner.setUsername("testUser");

        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    private Card createCard(String pan, User owner) {
        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setPan(pan);
        card.setOwner(owner);
        card.setBalance(BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiredAt(LocalDate.now().plusYears(1));
        return card;
    }

    private void mockAuth(String username, String role) {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(auth.getName()).thenReturn(username);
        lenient().doReturn(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .when(auth).getAuthorities();
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);

        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @Test
    void createCard_Success() {
        CreateCardDTO dto = new CreateCardDTO("1234567812345678", BigDecimal.TEN, ownerId, LocalDate.now().plusYears(1));

        when(cardRepository.existsByPan(dto.pan())).thenReturn(false);
        when(userService.getById(ownerId)).thenReturn(testOwner);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardDTO result = cardService.createCard(dto);

        assertNotNull(result);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void getCards_Admin_ReturnsAll() {
        mockAuth("admin", "ROLE_ADMIN");
        PageRequest pageable = PageRequest.of(0, 10);

        Card card = createCard("1111222233334444", testOwner);
        when(cardRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(card)));

        List<CardDTO> result = cardService.getCards(0, 10);

        assertFalse(result.isEmpty());
        assertEquals("************4444", result.get(0).maskedPan());
    }

    @Test
    void getCards_User_ReturnsOnlyOwned() {
        mockAuth("testUser", "ROLE_USER");
        PageRequest pageable = PageRequest.of(0, 10);

        Card card = createCard("1111222233334444", testOwner);
        when(cardRepository.findAllByOwnerUsername(eq("testUser"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(card)));

        List<CardDTO> result = cardService.getCards(0, 10);

        assertFalse(result.isEmpty());
        verify(cardRepository).findAllByOwnerUsername("testUser", pageable);
    }

    @Test
    void blockCard_Success() {
        mockAuth("testUser", "ROLE_USER");
        UUID cardId = UUID.randomUUID();
        Card card = createCard("1111222233334444", testOwner);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        CardDTO result = cardService.blockCard(cardId);

        assertEquals(CardStatus.BLOCKED, result.status());
    }

    @Test
    void transferMoney_Success() {
        mockAuth("testUser", "ROLE_USER");
        Card from = createCard("1111111111111111", testOwner);
        from.setBalance(new BigDecimal("1000"));

        Card to = createCard("2222222222222222", testOwner);
        to.setBalance(new BigDecimal("100"));

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(from));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(to));

        cardService.transferMoney(fromId, toId, new BigDecimal("300"));

        assertEquals(new BigDecimal("700"), from.getBalance());
        assertEquals(new BigDecimal("400"), to.getBalance());
    }

    @Test
    void transferMoney_InsufficientFunds() {
        mockAuth("testUser", "ROLE_USER");
        Card from = createCard("1111111111111111", testOwner);
        from.setBalance(new BigDecimal("50"));

        Card to = createCard("2222222222222222", testOwner); // Обязательно задаем владельца

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(from));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(to));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cardService.transferMoney(fromId, toId, new BigDecimal("100")));
        assertEquals("Недостаточно средств на карте", ex.getMessage());
    }
}