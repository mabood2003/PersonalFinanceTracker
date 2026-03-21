package com.moabood.financetracker.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateTransactionRequest {

    @NotNull
    private Long accountId;

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
    private LocalDate transactionDate;
}
