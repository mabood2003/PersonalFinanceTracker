package com.moabood.financetracker.recurring;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findAllByUserId(Long userId);
    Optional<RecurringTransaction> findByIdAndUserId(Long id, Long userId);
    List<RecurringTransaction> findAllByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}
