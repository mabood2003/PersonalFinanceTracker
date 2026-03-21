package com.moabood.financetracker.transaction;

import com.moabood.financetracker.account.Account;
import com.moabood.financetracker.account.AccountType;
import com.moabood.financetracker.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired TransactionRepository transactionRepository;

    @Test
    void shouldFindTransactionsByUserAndDateRange() {
        User user = createUser("repo@test.com");
        Account account = createAccount(user);

        createTransaction(user, account, TransactionType.EXPENSE, "100.00", LocalDate.of(2026, 3, 5));
        createTransaction(user, account, TransactionType.EXPENSE, "200.00", LocalDate.of(2026, 3, 15));
        createTransaction(user, account, TransactionType.EXPENSE, "50.00", LocalDate.of(2026, 2, 28));

        List<Transaction> results = transactionRepository.findByUserIdAndTransactionDateBetween(
                user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(results).hasSize(2);
    }

    @Test
    void shouldSumExpensesByCategory() {
        User user = createUser("sum@test.com");
        Account account = createAccount(user);

        createTransaction(user, account, TransactionType.EXPENSE, "75.00", LocalDate.of(2026, 3, 1));
        createTransaction(user, account, TransactionType.EXPENSE, "25.00", LocalDate.of(2026, 3, 10));

        BigDecimal sum = transactionRepository.sumByUserAndTypeAndDateRange(
                user.getId(), TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(sum).isEqualByComparingTo("100.00");
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setFirstName("Test");
        user.setLastName("User");
        return em.persistAndFlush(user);
    }

    private Account createAccount(User user) {
        Account account = new Account();
        account.setUser(user);
        account.setName("Test Account");
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency("CAD");
        return em.persistAndFlush(account);
    }

    private void createTransaction(User user, Account account, TransactionType type, String amount, LocalDate date) {
        Transaction t = new Transaction();
        t.setUser(user);
        t.setAccount(account);
        t.setType(type);
        t.setAmount(new BigDecimal(amount));
        t.setTransactionDate(date);
        em.persistAndFlush(t);
    }
}
