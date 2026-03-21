package com.moabood.financetracker.transaction;

import com.moabood.financetracker.account.Account;
import com.moabood.financetracker.account.AccountRepository;
import com.moabood.financetracker.category.Category;
import com.moabood.financetracker.category.CategoryRepository;
import com.moabood.financetracker.common.PagedResponse;
import com.moabood.financetracker.common.ResourceNotFoundException;
import com.moabood.financetracker.mapper.TransactionMapper;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    public PagedResponse<TransactionDto> getTransactions(TransactionFilterRequest filter, Pageable pageable) {
        User user = userService.getCurrentUser();
        Specification<Transaction> spec = TransactionSpecification.withFilters(user.getId(), filter);
        Page<Transaction> page = transactionRepository.findAll(spec, pageable);
        return new PagedResponse<>(page.map(transactionMapper::toDto));
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransaction(Long id) {
        User user = userService.getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return transactionMapper.toDto(transaction);
    }

    @Transactional
    public TransactionDto createTransaction(CreateTransactionRequest request) {
        if (request.getType() == TransactionType.TRANSFER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TRANSFER type is not supported in v1");
        }

        User user = userService.getCurrentUser();
        Account account = accountRepository.findByIdAndUserId(request.getAccountId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + request.getAccountId()));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setMerchant(request.getMerchant());
        transaction.setTransactionDate(request.getTransactionDate());

        applyBalanceEffect(account, request.getType(), request.getAmount());
        accountRepository.save(account);

        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionDto updateTransaction(Long id, CreateTransactionRequest request) {
        if (request.getType() == TransactionType.TRANSFER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TRANSFER type is not supported in v1");
        }

        User user = userService.getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        // Reverse the old effect
        Account oldAccount = transaction.getAccount();
        reverseBalanceEffect(oldAccount, transaction.getType(), transaction.getAmount());
        accountRepository.save(oldAccount);

        // Apply new effect
        Account newAccount = accountRepository.findByIdAndUserId(request.getAccountId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + request.getAccountId()));
        applyBalanceEffect(newAccount, request.getType(), request.getAmount());
        accountRepository.save(newAccount);

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        }

        transaction.setAccount(newAccount);
        transaction.setCategory(category);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setMerchant(request.getMerchant());
        transaction.setTransactionDate(request.getTransactionDate());

        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    @Transactional
    public void deleteTransaction(Long id) {
        User user = userService.getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }

        // Reverse the balance effect
        Account account = transaction.getAccount();
        reverseBalanceEffect(account, transaction.getType(), transaction.getAmount());
        accountRepository.save(account);

        transactionRepository.delete(transaction);
    }

    private void applyBalanceEffect(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().subtract(amount));
        } else if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(amount));
        }
    }

    private void reverseBalanceEffect(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().add(amount));
        } else if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(amount));
        }
    }
}
