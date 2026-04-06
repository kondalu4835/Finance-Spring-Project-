package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.dto.AuthDto;
import com.finance.entity.User;
import com.finance.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, and profile")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user (public — gets VIEWER role)")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDto.UserDto>> register(
            @Valid @RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", authService.register(request)));
    }

    @Operation(summary = "Login and receive JWT token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @Operation(summary = "Get current authenticated user profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDto.UserDto>> me(@AuthenticationPrincipal User user) {
        AuthDto.UserDto dto = AuthDto.UserDto.builder()
                .id(user.getId()).name(user.getName()).email(user.getEmail())
                .role(user.getRole()).active(user.isActive()).createdAt(user.getCreatedAt())
                .build();
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
