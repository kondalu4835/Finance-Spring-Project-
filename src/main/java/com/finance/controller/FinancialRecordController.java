package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.dto.FinancialRecordDto;
import com.finance.entity.RecordType;
import com.finance.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Financial Records", description = "CRUD for financial records")
@SecurityRequirement(name = "Bearer Auth")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    @Operation(summary = "List records with optional filters and pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<FinancialRecordDto.PageResponse>> list(
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                recordService.list(type, category, dateFrom, dateTo, page, size)));
    }

    @Operation(summary = "Get a single record by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialRecordDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(recordService.getById(id)));
    }

    @Operation(summary = "Create a new financial record — Admin only")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordDto.Response>> create(
            @Valid @RequestBody FinancialRecordDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Record created", recordService.create(request)));
    }

    @Operation(summary = "Partially update a record — Admin only")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FinancialRecordDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Record updated", recordService.update(id, request)));
    }

    @Operation(summary = "Soft-delete a record — Admin only")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        recordService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok("Record deleted successfully", null));
    }
}
