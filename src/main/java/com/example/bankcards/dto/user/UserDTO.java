package com.example.bankcards.dto.user;

import com.example.bankcards.entity.User;

import java.util.UUID;

public record UserDTO(
        UUID id,
        String username,
        String fullName
) {
    public UserDTO(User user) {
        this(user.getId(), user.getUsername(), user.getFullName());
    }
}
