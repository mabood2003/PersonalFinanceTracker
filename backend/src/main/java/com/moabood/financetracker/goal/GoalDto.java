package com.moabood.financetracker.goal;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Builder
public class GoalDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private double percentComplete;
    private BigDecimal amountRemaining;
    private LocalDate targetDate;
    private GoalStatus status;
    private String icon;
    private String color;
    private OffsetDateTime createdAt;
}
