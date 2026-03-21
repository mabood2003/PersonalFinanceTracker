package com.moabood.financetracker.transaction;

import com.moabood.financetracker.account.Account;
import com.moabood.financetracker.account.AccountRepository;
import com.moabood.financetracker.category.CategoryRepository;
import com.moabood.financetracker.common.ResourceNotFoundException;
import com.moabood.financetracker.mapper.TransactionMapper;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionMapper transactionMapper;
    @Mock private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        setId(user, 1L);

        account = new Account();
        account.setUser(user);
        account.setBalance(new BigDecimal("1000.00"));
        setId(account, 1L);
    }

    @Test
    void shouldCreateTransaction_andUpdateAccountBalance() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionMapper.toDto(any())).thenReturn(mock(TransactionDto.class));

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(1L);
        request.setType(TransactionType.EXPENSE);
        request.setAmount(new BigDecimal("150.00"));
        request.setTransactionDate(LocalDate.now());

        transactionService.createTransaction(request);

        assertThat(account.getBalance()).isEqualByComparingTo("850.00");
        verify(accountRepository).save(account);
    }

    @Test
    void shouldDeleteTransaction_andReverseAccountBalance() {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setAmount(new BigDecimal("200.00"));
        setId(transaction, 10L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(10L);

        assertThat(account.getBalance()).isEqualByComparingTo("1200.00");
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void shouldThrowResourceNotFound_whenTransactionDoesNotExist() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldThrowAccessDenied_whenUserDoesNotOwnTransaction() {
        User other = new User();
        setId(other, 2L);

        Transaction transaction = new Transaction();
        transaction.setUser(other);
        transaction.setAccount(account);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setAmount(BigDecimal.TEN);
        setId(transaction, 5L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.deleteTransaction(5L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // Reflection helper to set id on BaseEntity subclasses
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
