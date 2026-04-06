package com.finance.service;

import com.finance.dto.DashboardDto;
import com.finance.dto.FinancialRecordDto;
import com.finance.entity.RecordType;
import com.finance.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final FinancialRecordService recordService;

    public DashboardDto.Summary summary(LocalDate dateFrom, LocalDate dateTo) {
        BigDecimal income   = recordRepository.sumByType(RecordType.INCOME,  dateFrom, dateTo);
        BigDecimal expenses = recordRepository.sumByType(RecordType.EXPENSE, dateFrom, dateTo);
        long count          = recordRepository.countActive(dateFrom, dateTo);
        return DashboardDto.Summary.builder()
                .totalIncome(income)
                .totalExpenses(expenses)
                .netBalance(income.subtract(expenses))
                .recordCount(count)
                .build();
    }

    public List<DashboardDto.CategoryItem> categoryBreakdown(
            RecordType type, LocalDate dateFrom, LocalDate dateTo) {
        return recordRepository.categoryBreakdown(type, dateFrom, dateTo)
                .stream().map(row -> DashboardDto.CategoryItem.builder()
                        .category((String)    row[0])
                        .type(row[1].toString())
                        .count(((Number)      row[2]).longValue())
                        .total((BigDecimal)   row[3])
                        .build())
                .toList();
    }

    public List<DashboardDto.MonthlyTrend> monthlyTrends(Integer year) {
        return recordRepository.monthlyTrends(year)
                .stream().map(row -> {
                    BigDecimal income   = (BigDecimal) row[1];
                    BigDecimal expenses = (BigDecimal) row[2];
                    return DashboardDto.MonthlyTrend.builder()
                            .month((String)   row[0])
                            .income(income)
                            .expenses(expenses)
                            .net(income.subtract(expenses))
                            .build();
                }).toList();
    }

    public List<DashboardDto.WeeklyTrend> weeklyTrends(int weeks) {
        LocalDate since = LocalDate.now().minusWeeks(weeks);
        return recordRepository.weeklyTrends(since)
                .stream().map(row -> DashboardDto.WeeklyTrend.builder()
                        .week((String)      row[0])
                        .income((BigDecimal) row[1])
                        .expenses((BigDecimal) row[2])
                        .build())
                .toList();
    }

    public List<FinancialRecordDto.Response> recentActivity(int limit) {
        return recordRepository.findRecent(PageRequest.of(0, limit))
                .stream().map(r -> recordService.getById(r.getId()))
                .toList();
    }
}
