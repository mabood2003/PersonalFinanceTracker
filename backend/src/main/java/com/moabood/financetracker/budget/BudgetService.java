package com.moabood.financetracker.budget;

import com.moabood.financetracker.category.Category;
import com.moabood.financetracker.category.CategoryRepository;
import com.moabood.financetracker.common.ResourceNotFoundException;
import com.moabood.financetracker.mapper.BudgetMapper;
import com.moabood.financetracker.mapper.CategoryMapper;
import com.moabood.financetracker.transaction.TransactionRepository;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetMapper budgetMapper;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<BudgetProgressDto> getAllBudgetsWithProgress() {
        User user = userService.getCurrentUser();
        return budgetRepository.findAllByUserId(user.getId())
                .stream()
                .map(budget -> calculateProgress(budget, user.getId()))
                .toList();
    }

    @Transactional
    public BudgetDto createBudget(CreateBudgetRequest request) {
        User user = userService.getCurrentUser();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmountLimit(request.getAmountLimit());
        budget.setPeriod(request.getPeriod());
        budget.setStartDate(request.getStartDate());
        return budgetMapper.toDto(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetDto updateBudget(Long id, CreateBudgetRequest request) {
        User user = userService.getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        budget.setCategory(category);
        budget.setAmountLimit(request.getAmountLimit());
        budget.setPeriod(request.getPeriod());
        budget.setStartDate(request.getStartDate());
        return budgetMapper.toDto(budgetRepository.save(budget));
    }

    @Transactional
    public void deleteBudget(Long id) {
        User user = userService.getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        budgetRepository.delete(budget);
    }

    private BudgetProgressDto calculateProgress(Budget budget, Long userId) {
        LocalDate[] range = getPeriodRange(budget.getPeriod());
        LocalDate startDate = range[0];
        LocalDate endDate = range[1];

        BigDecimal spent = transactionRepository.sumExpensesByUserAndCategoryAndDateRange(
                userId, budget.getCategory().getId(), startDate, endDate);
        if (spent == null) spent = BigDecimal.ZERO;

        BigDecimal limit = budget.getAmountLimit();
        BigDecimal remaining = limit.subtract(spent);
        double percentUsed = limit.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                spent.multiply(BigDecimal.valueOf(100))
                     .divide(limit, 2, RoundingMode.HALF_UP)
                     .doubleValue();

        String status;
        if (percentUsed > 100) {
            status = "EXCEEDED";
        } else if (percentUsed > 75) {
            status = "WARNING";
        } else {
            status = "ON_TRACK";
        }

        return BudgetProgressDto.builder()
                .id(budget.getId())
                .category(categoryMapper.toDto(budget.getCategory()))
                .amountLimit(limit)
                .amountSpent(spent)
                .amountRemaining(remaining)
                .percentUsed(percentUsed)
                .period(budget.getPeriod())
                .startDate(budget.getStartDate())
                .status(status)
                .build();
    }

    private LocalDate[] getPeriodRange(BudgetPeriod period) {
        LocalDate now = LocalDate.now();
        if (period == BudgetPeriod.MONTHLY) {
            return new LocalDate[]{
                    now.with(TemporalAdjusters.firstDayOfMonth()),
                    now.with(TemporalAdjusters.lastDayOfMonth())
            };
        } else {
            return new LocalDate[]{
                    now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            };
        }
    }
}
