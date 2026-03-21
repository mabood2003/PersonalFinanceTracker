package com.moabood.financetracker.budget;

import com.moabood.financetracker.category.CategoryDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class BudgetProgressDto {
    private Long id;
    private CategoryDto category;
    private BigDecimal amountLimit;
    private BigDecimal amountSpent;
    private BigDecimal amountRemaining;
    private double percentUsed;
    private BudgetPeriod period;
    private LocalDate startDate;
    private String status; // ON_TRACK, WARNING, EXCEEDED
}
