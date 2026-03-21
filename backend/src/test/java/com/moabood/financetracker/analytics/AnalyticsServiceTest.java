package com.moabood.financetracker.analytics;

import com.moabood.financetracker.transaction.TransactionRepository;
import com.moabood.financetracker.transaction.TransactionType;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserService userService;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        try {
            var field = user.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void shouldReturnMonthlySummary() {
        when(transactionRepository.sumByUserAndTypeAndDateRange(eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(new BigDecimal("4500.00"));
        when(transactionRepository.sumByUserAndTypeAndDateRange(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(new BigDecimal("2847.33"));
        when(transactionRepository.countByUserAndDateRange(eq(1L), any(), any())).thenReturn(47L);
        when(transactionRepository.sumByCategory(anyLong(), any(), any(), any())).thenReturn(List.of());

        MonthlySummaryDto result = analyticsService.getMonthlySummary(2026, 3);

        assertThat(result.getTotalIncome()).isEqualByComparingTo("4500.00");
        assertThat(result.getTotalExpenses()).isEqualByComparingTo("2847.33");
        assertThat(result.getNetSavings()).isEqualByComparingTo("1652.67");
        assertThat(result.getTransactionCount()).isEqualTo(47);
    }

    @Test
    void shouldReturnCategoryBreakdown_sortedByAmount() {
        Object[] row1 = {1L, "Groceries", "🛒", "#4CAF50", new BigDecimal("623.45"), 12L};
        Object[] row2 = {2L, "Dining Out", "🍽️", "#FF9800", new BigDecimal("200.00"), 5L};

        when(transactionRepository.findCategoryBreakdown(anyLong(), any(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(row1, row2));

        List<CategoryBreakdownDto> result = analyticsService.getCategoryBreakdown(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), TransactionType.EXPENSE);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Groceries");
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("623.45");
        // Groceries = 623.45 / 823.45 * 100 ≈ 75.7%
        assertThat(result.get(0).getPercentage()).isGreaterThan(70);
    }
}
