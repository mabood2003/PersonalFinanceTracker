package com.moabood.financetracker.networth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NetWorthScheduler {

    private final NetWorthService netWorthService;

    /**
     * Takes a net worth snapshot for every user daily at midnight.
     * This creates a time-series that can be charted in the UI.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void takeDailySnapshot() {
        log.info("Running daily net worth snapshot...");
        netWorthService.takeSnapshotForAllUsers();
    }
}
