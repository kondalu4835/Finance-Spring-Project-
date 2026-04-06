package com.finance.controller;

import com.finance.dto.ApiResponse;
import com.finance.dto.DashboardDto;
import com.finance.dto.FinancialRecordDto;
import com.finance.entity.RecordType;
import com.finance.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Analytics and summary APIs — all authenticated roles")
@SecurityRequirement(name = "Bearer Auth")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Overall summary: total income, expenses, net balance, record count")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardDto.Summary>> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.summary(dateFrom, dateTo)));
    }

    @Operation(summary = "Category-wise totals, optionally filtered by type")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<DashboardDto.CategoryItem>>> categories(
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.categoryBreakdown(type, dateFrom, dateTo)));
    }

    @Operation(summary = "Monthly income vs expense trends, optionally filtered by year")
    @GetMapping("/trends/monthly")
    public ResponseEntity<ApiResponse<List<DashboardDto.MonthlyTrend>>> monthly(
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.monthlyTrends(year)));
    }

    @Operation(summary = "Weekly trends for the last N weeks (default 12)")
    @GetMapping("/trends/weekly")
    public ResponseEntity<ApiResponse<List<DashboardDto.WeeklyTrend>>> weekly(
            @RequestParam(defaultValue = "12") int weeks) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.weeklyTrends(weeks)));
    }

    @Operation(summary = "Most recent financial activity (default 10 records)")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<FinancialRecordDto.Response>>> recent(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.recentActivity(limit)));
    }
}
