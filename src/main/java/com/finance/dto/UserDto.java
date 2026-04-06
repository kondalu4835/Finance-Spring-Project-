package com.finance.dto;

import com.finance.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String email;
        private Role role;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotNull(message = "Role is required")
        private Role role;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private Role role;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageResponse {
        private java.util.List<Response> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
