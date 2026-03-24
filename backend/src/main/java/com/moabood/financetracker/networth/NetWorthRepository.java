package com.moabood.financetracker.networth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NetWorthRepository extends JpaRepository<NetWorthSnapshot, Long> {
    List<NetWorthSnapshot> findAllByUserIdAndSnapshotDateAfterOrderBySnapshotDateAsc(Long userId, LocalDate after);
    Optional<NetWorthSnapshot> findByUserIdAndSnapshotDate(Long userId, LocalDate snapshotDate);
}
