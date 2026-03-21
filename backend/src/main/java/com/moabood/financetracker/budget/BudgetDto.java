package com.moabood.financetracker.budget;

import com.moabood.financetracker.category.CategoryDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Builder
public class BudgetDto {
    private Long id;
    private CategoryDto category;
    private BigDecimal amountLimit;
    private BudgetPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private OffsetDateTime createdAt;
}
