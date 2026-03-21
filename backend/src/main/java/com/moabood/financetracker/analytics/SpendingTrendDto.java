package com.moabood.financetracker.analytics;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SpendingTrendDto {
    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
}
