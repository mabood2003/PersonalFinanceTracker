package com.moabood.financetracker.recurring;

import com.moabood.financetracker.category.CategoryDto;
import com.moabood.financetracker.transaction.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Builder
public class RecurringTransactionDto {
    private Long id;
    private Long accountId;
    private String accountName;
    private Long destinationAccountId;
    private String destinationAccountName;
    private CategoryDto category;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String merchant;
    private RecurringFrequency frequency;
    private LocalDate startDate;
    private LocalDate nextRunDate;
    private LocalDate endDate;
    private LocalDate lastRunDate;
    private boolean active;
    private OffsetDateTime createdAt;
}
