package com.moabood.financetracker.networth;

import com.moabood.financetracker.account.Account;
import com.moabood.financetracker.account.AccountRepository;
import com.moabood.financetracker.account.AccountType;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserRepository;
import com.moabood.financetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NetWorthService {

    private final NetWorthRepository netWorthRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public List<NetWorthSnapshotDto> getHistory(int days) {
        User user = userService.getCurrentUser();
        // Ensure today's snapshot exists so there's always at least one data point
        ensureTodaySnapshot(user);
        LocalDate since = LocalDate.now().minusDays(days);
        return netWorthRepository
                .findAllByUserIdAndSnapshotDateAfterOrderBySnapshotDateAsc(user.getId(), since)
                .stream()
                .map(s -> NetWorthSnapshotDto.builder()
                        .date(s.getSnapshotDate())
                        .totalAssets(s.getTotalAssets())
                        .totalLiabilities(s.getTotalLiabilities())
                        .netWorth(s.getNetWorth())
                        .build())
                .toList();
    }

    @Transactional
    public void takeSnapshotForAllUsers() {
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAll();
        for (User user : users) {
            takeSnapshotForUser(user, today);
        }
        log.info("Net worth snapshots taken for {} users on {}", users.size(), today);
    }

    @Transactional
    public void takeSnapshotForUser(User user, LocalDate date) {
        List<Account> accounts = accountRepository.findAllByUserId(user.getId());

        BigDecimal totalAssets = accounts.stream()
                .filter(a -> a.getAccountType() != AccountType.CREDIT_CARD)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLiabilities = accounts.stream()
                .filter(a -> a.getAccountType() == AccountType.CREDIT_CARD)
                .map(Account::getBalance)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);

        NetWorthSnapshot snapshot = netWorthRepository
                .findByUserIdAndSnapshotDate(user.getId(), date)
                .orElseGet(() -> {
                    NetWorthSnapshot s = new NetWorthSnapshot();
                    s.setUser(user);
                    s.setSnapshotDate(date);
                    return s;
                });

        snapshot.setTotalAssets(totalAssets);
        snapshot.setTotalLiabilities(totalLiabilities);
        snapshot.setNetWorth(netWorth);
        netWorthRepository.save(snapshot);
    }

    private void ensureTodaySnapshot(User user) {
        LocalDate today = LocalDate.now();
        if (netWorthRepository.findByUserIdAndSnapshotDate(user.getId(), today).isEmpty()) {
            takeSnapshotForUser(user, today);
        }
    }
}
