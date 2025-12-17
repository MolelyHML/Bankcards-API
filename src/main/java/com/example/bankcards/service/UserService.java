package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserService extends UserDetailsService {
    boolean existsByUsername(String username);

    User getById(UUID id);
}
