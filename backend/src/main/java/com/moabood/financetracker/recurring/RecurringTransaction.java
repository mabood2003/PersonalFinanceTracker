package com.moabood.financetracker.recurring;

import com.moabood.financetracker.account.Account;
import com.moabood.financetracker.category.Category;
import com.moabood.financetracker.common.BaseEntity;
import com.moabood.financetracker.transaction.TransactionType;
import com.moabood.financetracker.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_transactions")
@Getter
@Setter
@NoArgsConstructor
public class RecurringTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String merchant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RecurringFrequency frequency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_run_date", nullable = false)
    private LocalDate nextRunDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "last_run_date")
    private LocalDate lastRunDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "idempotency_prefix", nullable = false, length = 100)
    private String idempotencyPrefix;
}
