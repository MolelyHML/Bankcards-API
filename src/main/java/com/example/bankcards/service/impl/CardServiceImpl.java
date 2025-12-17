package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CreateCardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public List<CardDTO> getAllCards(Integer page, Integer size) {
        size = size == null ? 10 : size;
        final var pageable = PageRequest.of(page, size);
        return repository.findAll(pageable)
                .map(CardDTO::new)
                .getContent();
    }

    @Override
    @Transactional
    public CardDTO changeCardStatus(UUID cardId, CardStatus status) {
        final var card = repository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта с id: " + cardId + " не найдена"));

        card.setStatus(status);

        return new CardDTO(card);
    }
}
