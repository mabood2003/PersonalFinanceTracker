package com.moabood.financetracker.networth;

import com.moabood.financetracker.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "net_worth_snapshots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "snapshot_date"}))
@Getter
@Setter
@NoArgsConstructor
public class NetWorthSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_assets", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAssets;

    @Column(name = "total_liabilities", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalLiabilities;

    @Column(name = "net_worth", nullable = false, precision = 15, scale = 2)
    private BigDecimal netWorth;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
