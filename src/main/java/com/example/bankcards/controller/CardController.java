package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.dto.card.CreateCardDTO;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CreateCardDTO card) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(card));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/change-status")
    public ResponseEntity<CardDTO> changeCardStatus(@RequestParam UUID cardId,
                                                    @RequestParam CardStatus newStatus) {
        return ResponseEntity.ok(cardService.changeCardStatus(cardId, newStatus));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cardId}")
    public void deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<List<CardDTO>> getCards(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(cardService.getCards(page, size));
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{cardId}/block")
    public ResponseEntity<CardDTO> blockCard(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.getCardBalance(cardId));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestParam UUID fromCardId,
                                         @RequestParam UUID toCardId,
                                         @RequestParam BigDecimal amount) {
        cardService.transferMoney(fromCardId, toCardId, amount);
        return ResponseEntity.ok().build();
    }
}
