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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
        User user = userService.getCurrentUser();

        if (request.getType() == TransactionType.TRANSFER) {
            return createTransferTransaction(user, request);
        }

        Account account = getOwnedAccount(user.getId(), request.getAccountId());
        Category category = resolveOwnedCategory(user.getId(), request.getCategoryId());

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
        User user = userService.getCurrentUser();
        Transaction existing = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (existing.getType() == TransactionType.TRANSFER) {
            return updateExistingTransfer(user, existing, request);
        }

        // Existing regular transaction
        Account oldAccount = existing.getAccount();
        reverseBalanceEffect(oldAccount, existing.getType(), existing.getAmount());
        accountRepository.save(oldAccount);

        if (request.getType() == TransactionType.TRANSFER) {
            return convertRegularToTransfer(user, existing, request);
        }

        Account newAccount = getOwnedAccount(user.getId(), request.getAccountId());
        Category category = resolveOwnedCategory(user.getId(), request.getCategoryId());

        applyBalanceEffect(newAccount, request.getType(), request.getAmount());
        accountRepository.save(newAccount);

        existing.setAccount(newAccount);
        existing.setCategory(category);
        existing.setType(request.getType());
        existing.setAmount(request.getAmount());
        existing.setDescription(request.getDescription());
        existing.setMerchant(request.getMerchant());
        existing.setTransactionDate(request.getTransactionDate());
        existing.setIdempotencyKey(null);
        existing.setTransferGroupId(null);
        existing.setTransferLeg(null);

        return transactionMapper.toDto(transactionRepository.save(existing));
    }

    @Transactional
    public void deleteTransaction(Long id) {
        User user = userService.getCurrentUser();
        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (transaction.getType() == TransactionType.TRANSFER) {
            deleteTransferGroup(user.getId(), transaction);
            return;
        }

        Account account = transaction.getAccount();
        reverseBalanceEffect(account, transaction.getType(), transaction.getAmount());
        accountRepository.save(account);

        transactionRepository.delete(transaction);
    }

    private TransactionDto createTransferTransaction(User user, CreateTransactionRequest request) {
        String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());
        if (idempotencyKey == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "idempotencyKey is required for TRANSFER transactions");
        }

        validateTransferRequest(request);

        List<Transaction> existingByKey = transactionRepository
                .findAllByUserIdAndIdempotencyKey(user.getId(), idempotencyKey);

        if (!existingByKey.isEmpty()) {
            return handleExistingIdempotentTransfer(existingByKey, request, idempotencyKey);
        }

        Account sourceAccount = getOwnedAccount(user.getId(), request.getAccountId());
        Account destinationAccount = getOwnedAccount(user.getId(), request.getDestinationAccountId());

        UUID groupId = UUID.randomUUID();

        Transaction transferOut = new Transaction();
        transferOut.setUser(user);
        transferOut.setAccount(sourceAccount);
        transferOut.setCategory(null);
        transferOut.setType(TransactionType.TRANSFER);
        transferOut.setAmount(request.getAmount());
        transferOut.setDescription(request.getDescription());
        transferOut.setMerchant(request.getMerchant());
        transferOut.setTransactionDate(request.getTransactionDate());
        transferOut.setIdempotencyKey(idempotencyKey);
        transferOut.setTransferGroupId(groupId);
        transferOut.setTransferLeg(TransferLeg.OUT);

        Transaction transferIn = new Transaction();
        transferIn.setUser(user);
        transferIn.setAccount(destinationAccount);
        transferIn.setCategory(null);
        transferIn.setType(TransactionType.TRANSFER);
        transferIn.setAmount(request.getAmount());
        transferIn.setDescription(request.getDescription());
        transferIn.setMerchant(request.getMerchant());
        transferIn.setTransactionDate(request.getTransactionDate());
        transferIn.setIdempotencyKey(idempotencyKey);
        transferIn.setTransferGroupId(groupId);
        transferIn.setTransferLeg(TransferLeg.IN);

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        transactionRepository.saveAll(List.of(transferOut, transferIn));
        return transactionMapper.toDto(transferOut);
    }

    private TransactionDto handleExistingIdempotentTransfer(
            List<Transaction> existingByKey,
            CreateTransactionRequest request,
            String idempotencyKey
    ) {
        Transaction transferOut = existingByKey.stream()
                .filter(t -> t.getTransferLeg() == TransferLeg.OUT)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Idempotency key is already used by an invalid transfer payload: " + idempotencyKey));

        Transaction transferIn = existingByKey.stream()
                .filter(t -> t.getTransferLeg() == TransferLeg.IN)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Idempotency key is already used by an invalid transfer payload: " + idempotencyKey));

        boolean sameRequest = Objects.equals(transferOut.getAccount().getId(), request.getAccountId())
                && Objects.equals(transferIn.getAccount().getId(), request.getDestinationAccountId())
                && transferOut.getAmount().compareTo(request.getAmount()) == 0
                && Objects.equals(transferOut.getTransactionDate(), request.getTransactionDate())
                && Objects.equals(normalizeNullable(transferOut.getDescription()), normalizeNullable(request.getDescription()))
                && Objects.equals(normalizeNullable(transferOut.getMerchant()), normalizeNullable(request.getMerchant()));

        if (!sameRequest) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Idempotency key is already used by a different transfer request");
        }

        return transactionMapper.toDto(transferOut);
    }

    private TransactionDto updateExistingTransfer(User user, Transaction selectedLeg, CreateTransactionRequest request) {
        List<Transaction> group = getTransferGroup(user.getId(), selectedLeg.getTransferGroupId());
        Transaction transferOut = getTransferLeg(group, TransferLeg.OUT);
        Transaction transferIn = getTransferLeg(group, TransferLeg.IN);

        // Reverse old transfer effect
        Account oldSource = transferOut.getAccount();
        Account oldDestination = transferIn.getAccount();
        oldSource.setBalance(oldSource.getBalance().add(transferOut.getAmount()));
        oldDestination.setBalance(oldDestination.getBalance().subtract(transferIn.getAmount()));
        accountRepository.save(oldSource);
        accountRepository.save(oldDestination);

        if (request.getType() == TransactionType.TRANSFER) {
            validateTransferRequest(request);

            Account newSource = getOwnedAccount(user.getId(), request.getAccountId());
            Account newDestination = getOwnedAccount(user.getId(), request.getDestinationAccountId());
            String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());

            transferOut.setAccount(newSource);
            transferIn.setAccount(newDestination);
            transferOut.setAmount(request.getAmount());
            transferIn.setAmount(request.getAmount());
            transferOut.setDescription(request.getDescription());
            transferIn.setDescription(request.getDescription());
            transferOut.setMerchant(request.getMerchant());
            transferIn.setMerchant(request.getMerchant());
            transferOut.setTransactionDate(request.getTransactionDate());
            transferIn.setTransactionDate(request.getTransactionDate());

            if (idempotencyKey != null) {
                transferOut.setIdempotencyKey(idempotencyKey);
                transferIn.setIdempotencyKey(idempotencyKey);
            }

            newSource.setBalance(newSource.getBalance().subtract(request.getAmount()));
            newDestination.setBalance(newDestination.getBalance().add(request.getAmount()));
            accountRepository.save(newSource);
            accountRepository.save(newDestination);

            transactionRepository.saveAll(List.of(transferOut, transferIn));
            return transactionMapper.toDto(selectedLeg);
        }

        // Convert transfer -> regular transaction and keep the selected leg id
        Transaction companionLeg = selectedLeg.getId().equals(transferOut.getId()) ? transferIn : transferOut;
        transactionRepository.delete(companionLeg);

        Account account = getOwnedAccount(user.getId(), request.getAccountId());
        Category category = resolveOwnedCategory(user.getId(), request.getCategoryId());

        selectedLeg.setAccount(account);
        selectedLeg.setCategory(category);
        selectedLeg.setType(request.getType());
        selectedLeg.setAmount(request.getAmount());
        selectedLeg.setDescription(request.getDescription());
        selectedLeg.setMerchant(request.getMerchant());
        selectedLeg.setTransactionDate(request.getTransactionDate());
        selectedLeg.setIdempotencyKey(null);
        selectedLeg.setTransferGroupId(null);
        selectedLeg.setTransferLeg(null);

        applyBalanceEffect(account, request.getType(), request.getAmount());
        accountRepository.save(account);

        return transactionMapper.toDto(transactionRepository.save(selectedLeg));
    }

    private TransactionDto convertRegularToTransfer(User user, Transaction existing, CreateTransactionRequest request) {
        validateTransferRequest(request);

        Account sourceAccount = getOwnedAccount(user.getId(), request.getAccountId());
        Account destinationAccount = getOwnedAccount(user.getId(), request.getDestinationAccountId());
        String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());

        UUID groupId = UUID.randomUUID();

        existing.setAccount(sourceAccount);
        existing.setCategory(null);
        existing.setType(TransactionType.TRANSFER);
        existing.setAmount(request.getAmount());
        existing.setDescription(request.getDescription());
        existing.setMerchant(request.getMerchant());
        existing.setTransactionDate(request.getTransactionDate());
        existing.setIdempotencyKey(idempotencyKey);
        existing.setTransferGroupId(groupId);
        existing.setTransferLeg(TransferLeg.OUT);

        Transaction transferIn = new Transaction();
        transferIn.setUser(user);
        transferIn.setAccount(destinationAccount);
        transferIn.setCategory(null);
        transferIn.setType(TransactionType.TRANSFER);
        transferIn.setAmount(request.getAmount());
        transferIn.setDescription(request.getDescription());
        transferIn.setMerchant(request.getMerchant());
        transferIn.setTransactionDate(request.getTransactionDate());
        transferIn.setIdempotencyKey(idempotencyKey);
        transferIn.setTransferGroupId(groupId);
        transferIn.setTransferLeg(TransferLeg.IN);

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        transactionRepository.save(existing);
        transactionRepository.save(transferIn);

        return transactionMapper.toDto(existing);
    }

    private void deleteTransferGroup(Long userId, Transaction anyLeg) {
        List<Transaction> group = getTransferGroup(userId, anyLeg.getTransferGroupId());
        Transaction transferOut = getTransferLeg(group, TransferLeg.OUT);
        Transaction transferIn = getTransferLeg(group, TransferLeg.IN);

        Account source = transferOut.getAccount();
        Account destination = transferIn.getAccount();

        source.setBalance(source.getBalance().add(transferOut.getAmount()));
        destination.setBalance(destination.getBalance().subtract(transferIn.getAmount()));

        accountRepository.save(source);
        accountRepository.save(destination);
        transactionRepository.deleteAll(group);
    }

    private List<Transaction> getTransferGroup(Long userId, UUID groupId) {
        if (groupId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer transaction is missing transferGroupId");
        }
        List<Transaction> group = transactionRepository.findAllByUserIdAndTransferGroupId(userId, groupId);
        if (group.size() != 2) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transfer group is corrupted. Expected 2 ledger entries but found " + group.size());
        }
        return group;
    }

    private Transaction getTransferLeg(List<Transaction> group, TransferLeg leg) {
        return group.stream()
                .filter(t -> t.getTransferLeg() == leg)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Transfer group is missing the " + leg + " ledger leg"));
    }

    private Account getOwnedAccount(Long userId, Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    private Category resolveOwnedCategory(Long userId, Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        boolean allowed = category.isDefault()
                || (category.getUser() != null && Objects.equals(category.getUser().getId(), userId));

        if (!allowed) {
            throw new AccessDeniedException("Access denied");
        }

        return category;
    }

    private void validateTransferRequest(CreateTransactionRequest request) {
        if (request.getDestinationAccountId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "destinationAccountId is required for TRANSFER transactions");
        }
        if (Objects.equals(request.getAccountId(), request.getDestinationAccountId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source and destination accounts must be different");
        }
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String normalized = idempotencyKey.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
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
