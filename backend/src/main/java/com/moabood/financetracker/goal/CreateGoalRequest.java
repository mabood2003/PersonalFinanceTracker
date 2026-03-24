package com.moabood.financetracker.goal;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateGoalRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    @Positive
    private BigDecimal targetAmount;

    @DecimalMin("0.00")
    private BigDecimal currentAmount;

    private LocalDate targetDate;

    @Size(max = 10)
    private String icon;

    @Size(max = 20)
    private String color;

    private GoalStatus status;
}
