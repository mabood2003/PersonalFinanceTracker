package com.moabood.financetracker.transaction;

import com.moabood.financetracker.category.CategoryDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Builder
public class TransactionDto {
    private Long id;
    private Long accountId;
    private String accountName;
    private CategoryDto category;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String merchant;
    private LocalDate transactionDate;
    private OffsetDateTime createdAt;
}
