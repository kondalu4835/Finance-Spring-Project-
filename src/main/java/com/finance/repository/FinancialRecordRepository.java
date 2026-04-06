package com.finance.repository;

import com.finance.entity.FinancialRecord;
import com.finance.entity.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // Find by id (not soft-deleted)
    @Query("SELECT r FROM FinancialRecord r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<FinancialRecord> findActiveById(@Param("id") Long id);

    // Filtered list with pagination
    @Query("""
        SELECT r FROM FinancialRecord r
        WHERE r.deletedAt IS NULL
          AND (:type IS NULL OR r.type = :type)
          AND (:category IS NULL OR LOWER(r.category) = LOWER(:category))
          AND (:dateFrom IS NULL OR r.date >= :dateFrom)
          AND (:dateTo IS NULL OR r.date <= :dateTo)
        ORDER BY r.date DESC, r.id DESC
        """)
    Page<FinancialRecord> findAllWithFilters(
            @Param("type") RecordType type,
            @Param("category") String category,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Pageable pageable
    );

    // Dashboard: total by type
    @Query("""
        SELECT COALESCE(SUM(r.amount), 0)
        FROM FinancialRecord r
        WHERE r.deletedAt IS NULL AND r.type = :type
          AND (:dateFrom IS NULL OR r.date >= :dateFrom)
          AND (:dateTo IS NULL OR r.date <= :dateTo)
        """)
    BigDecimal sumByType(
            @Param("type") RecordType type,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    // Dashboard: total count
    @Query("""
        SELECT COUNT(r) FROM FinancialRecord r
        WHERE r.deletedAt IS NULL
          AND (:dateFrom IS NULL OR r.date >= :dateFrom)
          AND (:dateTo IS NULL OR r.date <= :dateTo)
        """)
    long countActive(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    // Dashboard: category breakdown
    @Query("""
        SELECT r.category, r.type, COUNT(r), SUM(r.amount)
        FROM FinancialRecord r
        WHERE r.deletedAt IS NULL
          AND (:type IS NULL OR r.type = :type)
          AND (:dateFrom IS NULL OR r.date >= :dateFrom)
          AND (:dateTo IS NULL OR r.date <= :dateTo)
        GROUP BY r.category, r.type
        ORDER BY SUM(r.amount) DESC
        """)
    List<Object[]> categoryBreakdown(
            @Param("type") RecordType type,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    // Dashboard: monthly trends
    @Query("""
        SELECT FORMATDATETIME(r.date, 'yyyy-MM') AS month,
               SUM(CASE WHEN r.type = 'INCOME'  THEN r.amount ELSE 0 END),
               SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount ELSE 0 END)
        FROM FinancialRecord r
        WHERE r.deletedAt IS NULL
          AND (:year IS NULL OR YEAR(r.date) = :year)
        GROUP BY FORMATDATETIME(r.date, 'yyyy-MM')
        ORDER BY month ASC
        """)
    List<Object[]> monthlyTrends(@Param("year") Integer year);

    // Dashboard: recent activity
    @Query("""
        SELECT r FROM FinancialRecord r
        WHERE r.deletedAt IS NULL
        ORDER BY r.date DESC, r.id DESC
        """)
    List<FinancialRecord> findRecent(Pageable pageable);

    // Dashboard: weekly trends (last N weeks)
    @Query("""
        SELECT FORMATDATETIME(r.date, 'yyyy-ww') AS week,
               SUM(CASE WHEN r.type = 'INCOME'  THEN r.amount ELSE 0 END),
               SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount ELSE 0 END)
        FROM FinancialRecord r
        WHERE r.deletedAt IS NULL
          AND r.date >= :since
        GROUP BY FORMATDATETIME(r.date, 'yyyy-ww')
        ORDER BY week ASC
        """)
    List<Object[]> weeklyTrends(@Param("since") LocalDate since);
}
