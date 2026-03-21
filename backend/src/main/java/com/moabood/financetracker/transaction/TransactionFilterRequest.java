package com.moabood.financetracker.transaction;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TransactionFilterRequest {
    private TransactionType type;
    private Long categoryId;
    private Long accountId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String search;
}
