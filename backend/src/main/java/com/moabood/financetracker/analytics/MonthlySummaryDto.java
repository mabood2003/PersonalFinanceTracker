package com.moabood.financetracker.analytics;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MonthlySummaryDto {
    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private double savingsRate;
    private long transactionCount;
    private TopCategoryDto topCategory;

    @Getter
    @Builder
    public static class TopCategoryDto {
        private String name;
        private BigDecimal amount;
    }
}
