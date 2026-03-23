package com.moabood.financetracker.recurring;

import com.moabood.financetracker.transaction.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateRecurringTransactionRequest {

    @NotNull
    private Long accountId;

    private Long destinationAccountId;

    private Long categoryId;

    @NotNull
    private TransactionType type;

    @NotNull
    @Positive
    private BigDecimal amount;

    @Size(max = 255)
    private String description;

    @Size(max = 100)
    private String merchant;

    @NotNull
    private RecurringFrequency frequency;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean active;
}
