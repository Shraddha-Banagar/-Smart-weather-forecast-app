package com.smartweather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartweather.dto.UserDtos.LoginRequest;
import com.smartweather.dto.UserDtos.UserResponse;
import com.smartweather.service.UserService;

import jakarta.validation.Valid;

/**
 * Phase 2 mock auth: {@code POST /api/auth/login {"username": "ada"}}
 * creates the user on first login, then returns the same row.
 */
@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        String username = request == null ? null : request.username();
        return ResponseEntity.ok(userService.login(username));
    }
}
