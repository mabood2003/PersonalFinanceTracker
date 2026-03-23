package com.moabood.financetracker.recurring;

import com.moabood.financetracker.account.Account;
import com.moabood.financetracker.account.AccountRepository;
import com.moabood.financetracker.category.CategoryRepository;
import com.moabood.financetracker.mapper.CategoryMapper;
import com.moabood.financetracker.transaction.TransactionDto;
import com.moabood.financetracker.transaction.TransactionService;
import com.moabood.financetracker.transaction.TransactionType;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock private RecurringTransactionRepository recurringTransactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;
    @Mock private UserService userService;
    @Mock private TransactionService transactionService;

    @InjectMocks
    private RecurringTransactionService recurringTransactionService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("recurring@test.com");
        setId(user, 1L);

        account = new Account();
        account.setUser(user);
        account.setName("Chequing");
        account.setBalance(new BigDecimal("1000.00"));
        setId(account, 10L);
    }

    @Test
    void shouldProcessDueDailyRecurringTransactions_andAdvanceNextRunDate() {
        LocalDate today = LocalDate.now();

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUser(user);
        recurring.setAccount(account);
        recurring.setType(TransactionType.EXPENSE);
        recurring.setAmount(new BigDecimal("25.00"));
        recurring.setFrequency(RecurringFrequency.DAILY);
        recurring.setStartDate(today.minusDays(1));
        recurring.setNextRunDate(today.minusDays(1));
        recurring.setActive(true);
        recurring.setIdempotencyPrefix("rec-1");

        when(recurringTransactionRepository.findAllByActiveTrueAndNextRunDateLessThanEqual(today))
                .thenReturn(List.of(recurring));
        when(transactionService.createTransactionForUser(eq(user), any()))
                .thenReturn(mock(TransactionDto.class));

        recurringTransactionService.processDueRecurringTransactions();

        assertThat(recurring.getLastRunDate()).isEqualTo(today);
        assertThat(recurring.getNextRunDate()).isEqualTo(today.plusDays(1));
        verify(transactionService, times(2)).createTransactionForUser(eq(user), any());
        verify(recurringTransactionRepository).save(recurring);
    }

    @Test
    void shouldDeactivateRecurringTransaction_afterEndDateIsReached() {
        LocalDate today = LocalDate.now();

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUser(user);
        recurring.setAccount(account);
        recurring.setType(TransactionType.INCOME);
        recurring.setAmount(new BigDecimal("100.00"));
        recurring.setFrequency(RecurringFrequency.WEEKLY);
        recurring.setStartDate(today);
        recurring.setNextRunDate(today);
        recurring.setEndDate(today);
        recurring.setActive(true);
        recurring.setIdempotencyPrefix("rec-2");

        when(recurringTransactionRepository.findAllByActiveTrueAndNextRunDateLessThanEqual(today))
                .thenReturn(List.of(recurring));
        when(transactionService.createTransactionForUser(eq(user), any()))
                .thenReturn(mock(TransactionDto.class));

        recurringTransactionService.processDueRecurringTransactions();

        assertThat(recurring.getLastRunDate()).isEqualTo(today);
        assertThat(recurring.getNextRunDate()).isEqualTo(today.plusWeeks(1));
        assertThat(recurring.isActive()).isFalse();
        verify(transactionService, times(1)).createTransactionForUser(eq(user), any());
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            try {
                var field = entity.getClass().getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
