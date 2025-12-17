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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CardDTO>> getAllCards(@RequestParam Integer page,
                                                     @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(cardService.getAllCards(page, size));
    }
}
