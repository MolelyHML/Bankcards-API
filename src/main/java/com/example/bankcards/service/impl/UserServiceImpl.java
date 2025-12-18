package com.example.bankcards.service.impl;

import com.example.bankcards.dto.user.UserDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByUsername(String username) {
        final var user = repository.findByUsername(username).orElse(null);
        return user == null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с именем " + username + " не найден"));
    }

    @Override
    public User getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с id: " + id + " не найден"));
    }

    @Override
    public UserDTO getUserByIdOrMe(UUID id) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        User user;

        if (id == null) {
            String currentUsername = auth.getName();
            user = repository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден в базе данных"));
        } else {
            if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")))
                throw new AuthorizationDeniedException("Пользователь с ROLE_USER не имеет прав на выполнение данного запроса.");
            user = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Пользователь с id: " + id + " не найден"));
        }

        return new UserDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers(Integer page, Integer size) {
        size = size == null ? 10 : size;

        final var pageable = PageRequest.of(page, size);

        return repository.findAll(pageable)
                .map(UserDTO::new)
                .getContent();
    }
}
