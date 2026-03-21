package com.moabood.financetracker.budget;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateBudgetRequest {

    @NotNull
    private Long categoryId;

    @NotNull
    @Positive
    private BigDecimal amountLimit;

    @NotNull
    private BudgetPeriod period;

    @NotNull
    private LocalDate startDate;
}
