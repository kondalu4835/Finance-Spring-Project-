package com.finance.dto;

import com.finance.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class LoginResponse {
        private String token;
        private String type = "Bearer";
        private UserDto user;
    }

    @Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UserDto {
        private Long id;
        private String name;
        private String email;
        private Role role;
        private boolean active;
        private java.time.LocalDateTime createdAt;
    }
}
