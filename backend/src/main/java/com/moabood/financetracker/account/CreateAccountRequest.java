package com.moabood.financetracker.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateAccountRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private AccountType accountType;

    @NotNull
    private BigDecimal balance;

    @Size(min = 3, max = 3)
    private String currency = "CAD";
}
