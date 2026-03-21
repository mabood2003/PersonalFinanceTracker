package com.moabood.financetracker.analytics;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CategoryBreakdownDto {
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal totalAmount;
    private double percentage;
    private long transactionCount;
}
