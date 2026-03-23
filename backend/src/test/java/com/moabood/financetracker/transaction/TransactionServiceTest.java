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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
        when(transactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(10L);

        assertThat(account.getBalance()).isEqualByComparingTo("1200.00");
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void shouldThrowResourceNotFound_whenTransactionDoesNotExist() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldThrowResourceNotFound_whenUserDoesNotOwnTransaction() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldCreateTransferTransaction_withDoubleEntryAndBalanceUpdates() {
        Account destination = new Account();
        destination.setUser(user);
        destination.setBalance(new BigDecimal("300.00"));
        setId(destination, 2L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(destination));
        when(transactionRepository.findAllByUserIdAndIdempotencyKey(1L, "transfer-key-1")).thenReturn(List.of());
        when(transactionMapper.toDto(any())).thenReturn(mock(TransactionDto.class));

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setType(TransactionType.TRANSFER);
        request.setAmount(new BigDecimal("125.00"));
        request.setTransactionDate(LocalDate.of(2026, 3, 23));
        request.setIdempotencyKey("transfer-key-1");

        transactionService.createTransaction(request);

        assertThat(account.getBalance()).isEqualByComparingTo("875.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("425.00");
        verify(transactionRepository).saveAll(any());
    }

    @Test
    void shouldReturnExistingTransfer_whenIdempotencyKeyIsReusedWithSamePayload() {
        Account destination = new Account();
        destination.setUser(user);
        destination.setBalance(new BigDecimal("300.00"));
        setId(destination, 2L);

        Transaction transferOut = new Transaction();
        transferOut.setUser(user);
        transferOut.setAccount(account);
        transferOut.setType(TransactionType.TRANSFER);
        transferOut.setTransferLeg(TransferLeg.OUT);
        transferOut.setAmount(new BigDecimal("50.00"));
        transferOut.setTransactionDate(LocalDate.of(2026, 3, 23));
        transferOut.setDescription("Move to savings");
        transferOut.setMerchant("Internal");

        Transaction transferIn = new Transaction();
        transferIn.setUser(user);
        transferIn.setAccount(destination);
        transferIn.setType(TransactionType.TRANSFER);
        transferIn.setTransferLeg(TransferLeg.IN);
        transferIn.setAmount(new BigDecimal("50.00"));
        transferIn.setTransactionDate(LocalDate.of(2026, 3, 23));
        transferIn.setDescription("Move to savings");
        transferIn.setMerchant("Internal");

        TransactionDto existingDto = mock(TransactionDto.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findAllByUserIdAndIdempotencyKey(1L, "transfer-key-2"))
                .thenReturn(List.of(transferOut, transferIn));
        when(transactionMapper.toDto(transferOut)).thenReturn(existingDto);

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setType(TransactionType.TRANSFER);
        request.setAmount(new BigDecimal("50.00"));
        request.setTransactionDate(LocalDate.of(2026, 3, 23));
        request.setDescription("Move to savings");
        request.setMerchant("Internal");
        request.setIdempotencyKey("transfer-key-2");

        TransactionDto result = transactionService.createTransaction(request);

        assertThat(result).isSameAs(existingDto);
        verify(transactionRepository, never()).saveAll(any());
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