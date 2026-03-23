package com.moabood.financetracker.budget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetCycleScheduler {

    private final BudgetRepository budgetRepository;

    @Scheduled(cron = "${app.scheduling.budget-cycles-cron:0 10 1 * * *}")
    @Transactional
    public void rolloverBudgetCycles() {
        LocalDate today = LocalDate.now();
        List<Budget> budgets = budgetRepository.findAll();

        int updated = 0;
        for (Budget budget : budgets) {
            if (!budget.isAutoRenew()) {
                continue;
            }

            LocalDate start = budget.getStartDate();
            LocalDate end = budget.getEndDate() != null
                    ? budget.getEndDate()
                    : resolveCycleEndDate(start, budget.getPeriod());

            boolean changed = budget.getEndDate() == null;

            while (end.isBefore(today)) {
                start = nextCycleStart(start, budget.getPeriod());
                end = resolveCycleEndDate(start, budget.getPeriod());
                changed = true;
            }

            if (changed) {
                budget.setStartDate(start);
                budget.setEndDate(end);
                updated++;
            }
        }

        if (updated > 0) {
            log.info("Rolled over {} budget cycle(s)", updated);
        }
    }

    private LocalDate nextCycleStart(LocalDate start, BudgetPeriod period) {
        if (period == BudgetPeriod.MONTHLY) {
            return start.plusMonths(1);
        }
        return start.plusWeeks(1);
    }

    private LocalDate resolveCycleEndDate(LocalDate start, BudgetPeriod period) {
        if (period == BudgetPeriod.MONTHLY) {
            return start.plusMonths(1).minusDays(1);
        }
        return start.plusDays(6);
    }
}
