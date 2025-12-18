package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CreateCardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository repository;
    private final UserService userService;

    public CardServiceImpl(CardRepository repository,
                           UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public CardDTO createCard(CreateCardDTO createDTO) {
        if(repository.existsByPan(createDTO.pan()))
            throw new RuntimeException("Карта с таким номером уже зарегистрирована");

        final var card = new Card();
        card.setPan(createDTO.pan());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(createDTO.balance());
        card.setExpiredAt(createDTO.expiredAt());
        card.setOwner(userService.getById(createDTO.ownerId()));

        return new CardDTO(repository.save(card));
    }

    @Override
    @Transactional
    public void deleteCard(UUID cardId) {
        repository.deleteById(cardId);
    }

    @Override
    @Transactional
    public CardDTO changeCardStatus(UUID cardId, CardStatus status) {
        final var card = repository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта с id: " + cardId + " не найдена"));

        card.setStatus(status);

        return new CardDTO(card);
    }

    @Override
    public List<CardDTO> getCards(Integer page, Integer size) {
        size = size == null ? 10 : size;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Pageable pageable = PageRequest.of(page, size);

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return repository.findAll(pageable).map(CardDTO::new).getContent();
        }

        return repository.findAllByOwnerUsername(auth.getName(), pageable)
                .map(CardDTO::new)
                .getContent();
    }

    @Override
    @Transactional
    public CardDTO blockCard(UUID cardId) {
        Card card = getOwnedCardOrThrow(cardId);
        card.setStatus(CardStatus.BLOCKED);
        return new CardDTO(card);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(UUID cardId) {
        return getOwnedCardOrThrow(cardId).getBalance();
    }

    @Override
    @Transactional
    public void transferMoney(UUID fromCardId, UUID toCardId, BigDecimal amount) {
        Card fromCard = getOwnedCardOrThrow(fromCardId);

        Card toCard = getOwnedCardOrThrow(toCardId);

        if (fromCard.getStatus() != CardStatus.ACTIVE)
            throw new RuntimeException("Карта отправителя недоступна");
        if (fromCard.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Недостаточно средств на карте");


        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        repository.saveAll(List.of(fromCard, toCard));
    }

    private Card getOwnedCardOrThrow(UUID cardId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Card card = repository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        if (!isAdmin && !card.getOwner().getUsername().equals(auth.getName()))
            throw new RuntimeException("Доступ запрещен: вы не владелец этой карты");
        return card;
    }
}
