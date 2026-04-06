package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.dto.UserDto;
import com.finance.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users", description = "User management — Admin only")
@SecurityRequirement(name = "Bearer Auth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "List all users with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<UserDto.PageResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(userService.list(page, size)));
    }

    @Operation(summary = "Create a user with any role")
    @PostMapping
    public ResponseEntity<ApiResponse<UserDto.Response>> create(
            @Valid @RequestBody UserDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User created", userService.create(request)));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getById(id)));
    }

    @Operation(summary = "Update user name, role, or status")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto.Response>> update(
            @PathVariable Long id,
            @RequestBody UserDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User updated", userService.update(id, request)));
    }
}
