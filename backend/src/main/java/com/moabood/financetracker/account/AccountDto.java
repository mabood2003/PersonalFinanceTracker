package com.moabood.financetracker.account;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class AccountDto {
    private Long id;
    private String name;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private OffsetDateTime createdAt;
}
