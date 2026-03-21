package com.moabood.financetracker.analytics;

import com.moabood.financetracker.transaction.TransactionRepository;
import com.moabood.financetracker.transaction.TransactionType;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public MonthlySummaryDto getMonthlySummary(int year, int month) {
        User user = userService.getCurrentUser();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAndDateRange(
                user.getId(), TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpenses = transactionRepository.sumByUserAndTypeAndDateRange(
                user.getId(), TransactionType.EXPENSE, startDate, endDate);
        Long count = transactionRepository.countByUserAndDateRange(user.getId(), startDate, endDate);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);
        double savingsRate = totalIncome.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                netSavings.multiply(BigDecimal.valueOf(100))
                          .divide(totalIncome, 1, RoundingMode.HALF_UP)
                          .doubleValue();

        // Top category by expenses
        List<Object[]> topCategories = transactionRepository.sumByCategory(
                user.getId(), TransactionType.EXPENSE, startDate, endDate);

        MonthlySummaryDto.TopCategoryDto topCategory = null;
        if (!topCategories.isEmpty()) {
            Object[] row = topCategories.get(0);
            topCategory = MonthlySummaryDto.TopCategoryDto.builder()
                    .name((String) row[0])
                    .amount((BigDecimal) row[1])
                    .build();
        }

        return MonthlySummaryDto.builder()
                .year(year)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .savingsRate(savingsRate)
                .transactionCount(count != null ? count : 0)
                .topCategory(topCategory)
                .build();
    }

    public List<CategoryBreakdownDto> getCategoryBreakdown(LocalDate startDate, LocalDate endDate, TransactionType type) {
        User user = userService.getCurrentUser();
        List<Object[]> rows = transactionRepository.findCategoryBreakdown(
                user.getId(), type, startDate, endDate);

        BigDecimal total = rows.stream()
                .map(r -> (BigDecimal) r[4])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(row -> {
            BigDecimal amount = (BigDecimal) row[4];
            double percentage = total.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                    amount.multiply(BigDecimal.valueOf(100))
                          .divide(total, 1, RoundingMode.HALF_UP)
                          .doubleValue();
            return CategoryBreakdownDto.builder()
                    .categoryId((Long) row[0])
                    .categoryName((String) row[1])
                    .categoryIcon((String) row[2])
                    .categoryColor((String) row[3])
                    .totalAmount(amount)
                    .percentage(percentage)
                    .transactionCount(((Number) row[5]).longValue())
                    .build();
        }).toList();
    }

    public List<SpendingTrendDto> getSpendingTrend(int months) {
        User user = userService.getCurrentUser();
        LocalDate startDate = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .minusMonths(months - 1L);

        List<Object[]> rows = transactionRepository.findMonthlyTrend(user.getId(), startDate);

        return rows.stream().map(row -> SpendingTrendDto.builder()
                .year(((Number) row[0]).intValue())
                .month(((Number) row[1]).intValue())
                .totalIncome(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO)
                .totalExpenses(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO)
                .build()).toList();
    }
}
