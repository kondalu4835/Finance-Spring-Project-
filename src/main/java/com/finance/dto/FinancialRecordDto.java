package com.finance.dto;

import com.finance.entity.RecordType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FinancialRecordDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        @NotNull(message = "Type is required (INCOME or EXPENSE)")
        private RecordType type;

        @NotBlank(message = "Category is required")
        private String category;

        @NotNull(message = "Date is required")
        private LocalDate date;

        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        private String notes;
    }

    @Data
    public static class UpdateRequest {
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private RecordType type;

        @Size(min = 1, message = "Category cannot be blank")
        private String category;

        private LocalDate date;

        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private BigDecimal amount;
        private RecordType type;
        private String category;
        private LocalDate date;
        private String notes;
        private String createdByName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageResponse {
        private List<Response> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
