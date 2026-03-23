package com.moabood.financetracker.recurring;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "${app.scheduling.recurring-transactions-cron:0 0 1 * * *}")
    public void processRecurringTransactions() {
        recurringTransactionService.processDueRecurringTransactions();
    }
}
