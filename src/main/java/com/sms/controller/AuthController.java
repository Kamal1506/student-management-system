package com.sms.controller;

import com.sms.dto.LoginRequest;
import com.sms.dto.LoginResponse;
import com.sms.dto.RegisterRequest;
import com.sms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─── POST /api/auth/login ─────────────────────────────────────────────────
    // Public endpoint — no token required
    //
    // Request body:  { "username": "john", "password": "secret123" }
    // Response:      { "token": "eyJ...", "role": "ADMIN", "username": "john" }
    //
    // Frontend stores the token in localStorage, reads the role,
    // then redirects to admin-dashboard / teacher-dashboard / student-dashboard

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ─── POST /api/auth/register ──────────────────────────────────────────────
    // Creates a new user account (username + password + role)
    // In a real system, restrict this to ADMIN only via SecurityConfig
    //
    // Request body:  { "username": "jane", "password": "pass123", "role": "STUDENT" }
    // Response:      201 Created  +  "User registered successfully: jane"

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}