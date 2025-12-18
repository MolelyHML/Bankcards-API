package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserDTO;
import com.example.bankcards.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {
    boolean existsByUsername(String username);

    User getById(UUID id);

    UserDTO getUserByIdOrMe(UUID id);

    List<UserDTO> getAllUsers(Integer page, Integer size);
}
