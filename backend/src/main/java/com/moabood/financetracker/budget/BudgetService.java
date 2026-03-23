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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

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
        Category category = getOwnedCategory(user.getId(), request.getCategoryId());

        LocalDate resolvedEndDate = request.getEndDate() != null
                ? request.getEndDate()
                : resolveCycleEndDate(request.getStartDate(), request.getPeriod());

        validateBudgetDates(request.getStartDate(), resolvedEndDate);

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmountLimit(request.getAmountLimit());
        budget.setPeriod(request.getPeriod());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(resolvedEndDate);
        budget.setAutoRenew(request.getAutoRenew() == null || request.getAutoRenew());

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

        Category category = getOwnedCategory(user.getId(), request.getCategoryId());
        LocalDate resolvedEndDate = request.getEndDate() != null
                ? request.getEndDate()
                : resolveCycleEndDate(request.getStartDate(), request.getPeriod());
        validateBudgetDates(request.getStartDate(), resolvedEndDate);

        budget.setCategory(category);
        budget.setAmountLimit(request.getAmountLimit());
        budget.setPeriod(request.getPeriod());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(resolvedEndDate);
        budget.setAutoRenew(request.getAutoRenew() == null || request.getAutoRenew());

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
        LocalDate startDate = budget.getStartDate();
        LocalDate endDate = budget.getEndDate() != null
                ? budget.getEndDate()
                : resolveCycleEndDate(startDate, budget.getPeriod());

        BigDecimal spent = transactionRepository.sumExpensesByUserAndCategoryAndDateRange(
                userId, budget.getCategory().getId(), startDate, endDate);
        if (spent == null) {
            spent = BigDecimal.ZERO;
        }

        BigDecimal limit = budget.getAmountLimit();
        BigDecimal remaining = limit.subtract(spent);
        double percentUsed = limit.compareTo(BigDecimal.ZERO) == 0
                ? 0
                : spent.multiply(BigDecimal.valueOf(100))
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
                .startDate(startDate)
                .endDate(endDate)
                .autoRenew(budget.isAutoRenew())
                .status(status)
                .build();
    }

    private Category getOwnedCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        boolean allowed = category.isDefault()
                || (category.getUser() != null && Objects.equals(category.getUser().getId(), userId));

        if (!allowed) {
            throw new AccessDeniedException("Access denied");
        }

        return category;
    }

    private void validateBudgetDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "endDate cannot be before startDate");
        }
    }

    private LocalDate resolveCycleEndDate(LocalDate startDate, BudgetPeriod period) {
        if (period == BudgetPeriod.MONTHLY) {
            return startDate.plusMonths(1).minusDays(1);
        }
        return startDate.plusDays(6);
    }
}