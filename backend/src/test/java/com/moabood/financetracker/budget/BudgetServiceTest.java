package com.moabood.financetracker.budget;

import com.moabood.financetracker.category.Category;
import com.moabood.financetracker.category.CategoryRepository;
import com.moabood.financetracker.mapper.BudgetMapper;
import com.moabood.financetracker.mapper.CategoryMapper;
import com.moabood.financetracker.transaction.TransactionRepository;
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
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private BudgetMapper budgetMapper;
    @Mock private CategoryMapper categoryMapper;
    @Mock private UserService userService;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Budget budget;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        setId(user, 1L);

        category = new Category();
        setId(category, 1L);
        category.setName("Groceries");

        budget = new Budget();
        setId(budget, 1L);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmountLimit(new BigDecimal("500.00"));
        budget.setPeriod(BudgetPeriod.MONTHLY);
        budget.setStartDate(LocalDate.now().withDayOfMonth(1));
    }

    @Test
    void shouldCalculateBudgetProgress() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(budgetRepository.findAllByUserId(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByUserAndCategoryAndDateRange(anyLong(), anyLong(), any(), any()))
                .thenReturn(new BigDecimal("250.00"));
        when(categoryMapper.toDto(any())).thenReturn(null);

        List<BudgetProgressDto> result = budgetService.getAllBudgetsWithProgress();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmountSpent()).isEqualByComparingTo("250.00");
        assertThat(result.get(0).getAmountRemaining()).isEqualByComparingTo("250.00");
        assertThat(result.get(0).getPercentUsed()).isEqualTo(50.0);
    }

    @Test
    void shouldReturnWarningStatus_whenOver75Percent() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(budgetRepository.findAllByUserId(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByUserAndCategoryAndDateRange(anyLong(), anyLong(), any(), any()))
                .thenReturn(new BigDecimal("400.00"));
        when(categoryMapper.toDto(any())).thenReturn(null);

        List<BudgetProgressDto> result = budgetService.getAllBudgetsWithProgress();

        assertThat(result.get(0).getStatus()).isEqualTo("WARNING");
    }

    @Test
    void shouldReturnExceededStatus_whenOver100Percent() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(budgetRepository.findAllByUserId(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByUserAndCategoryAndDateRange(anyLong(), anyLong(), any(), any()))
                .thenReturn(new BigDecimal("550.00"));
        when(categoryMapper.toDto(any())).thenReturn(null);

        List<BudgetProgressDto> result = budgetService.getAllBudgetsWithProgress();

        assertThat(result.get(0).getStatus()).isEqualTo("EXCEEDED");
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            try {
                var field = entity.getClass().getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
