package com.example.bankcards.controller;

import com.example.bankcards.dto.user.UserDTO;
import com.example.bankcards.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("/get")
    public ResponseEntity<UserDTO> getUserById(@RequestParam(required = false) UUID id) {
        return ResponseEntity.ok(userService.getUserByIdOrMe(id));
    }
}
