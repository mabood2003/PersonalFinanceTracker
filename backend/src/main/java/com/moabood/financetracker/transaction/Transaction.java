package com.moabood.financetracker.transaction;

import com.moabood.financetracker.account.Account;
import com.moabood.financetracker.category.Category;
import com.moabood.financetracker.common.BaseEntity;
import com.moabood.financetracker.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

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

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "transfer_group_id")
    private UUID transferGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_leg", length = 3)
    private TransferLeg transferLeg;
}
