package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.RegisterRequest;
import com.example.bankcards.security.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/registration")
public class RegisterController {

    private final AuthService authService;

    public RegisterController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public void register(@RequestBody RegisterRequest req) {
        authService.register(req);
    }
}
