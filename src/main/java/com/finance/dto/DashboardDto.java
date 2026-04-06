package com.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netBalance;
        private long recordCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryItem {
        private String category;
        private String type;
        private long count;
        private BigDecimal total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private String month;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyTrend {
        private String week;
        private BigDecimal income;
        private BigDecimal expenses;
    }
}
