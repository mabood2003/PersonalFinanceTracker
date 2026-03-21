package com.moabood.financetracker.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId " +
           "AND t.type = 'EXPENSE' AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumExpensesByUserAndCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumByUserAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    Long countByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.type = :type AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
           "AND t.category IS NOT NULL GROUP BY t.category.id, t.category.name ORDER BY SUM(t.amount) DESC")
    List<Object[]> sumByCategory(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t.category.id, t.category.name, t.category.icon, t.category.color, " +
           "SUM(t.amount), COUNT(t) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.type = :type AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
           "AND t.category IS NOT NULL GROUP BY t.category.id, t.category.name, t.category.icon, t.category.color " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> findCategoryBreakdown(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), " +
           "SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), " +
           "SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) " +
           "FROM Transaction t WHERE t.user.id = :userId AND t.transactionDate >= :startDate " +
           "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) " +
           "ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> findMonthlyTrend(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate);

    List<Transaction> findTop5ByUserIdOrderByTransactionDateDescCreatedAtDesc(Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
