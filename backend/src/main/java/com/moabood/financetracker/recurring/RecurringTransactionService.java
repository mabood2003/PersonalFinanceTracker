package com.moabood.financetracker.recurring;

import com.moabood.financetracker.account.Account;
import com.moabood.financetracker.account.AccountRepository;
import com.moabood.financetracker.category.Category;
import com.moabood.financetracker.category.CategoryRepository;
import com.moabood.financetracker.common.ResourceNotFoundException;
import com.moabood.financetracker.mapper.CategoryMapper;
import com.moabood.financetracker.transaction.CreateTransactionRequest;
import com.moabood.financetracker.transaction.TransactionService;
import com.moabood.financetracker.transaction.TransactionType;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserService userService;
    private final TransactionService transactionService;

    @Transactional(readOnly = true)
    public List<RecurringTransactionDto> getAll() {
        User user = userService.getCurrentUser();
        return recurringTransactionRepository.findAllByUserId(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public RecurringTransactionDto create(CreateRecurringTransactionRequest request) {
        User user = userService.getCurrentUser();
        validateRequest(request);

        Account sourceAccount = getOwnedAccount(user.getId(), request.getAccountId());
        Account destinationAccount = resolveDestinationAccount(user.getId(), request);
        Category category = resolveCategory(user.getId(), request);

        RecurringTransaction recurring = new RecurringTransaction();
        recurring.setUser(user);
        recurring.setAccount(sourceAccount);
        recurring.setDestinationAccount(destinationAccount);
        recurring.setCategory(category);
        recurring.setType(request.getType());
        recurring.setAmount(request.getAmount());
        recurring.setDescription(request.getDescription());
        recurring.setMerchant(request.getMerchant());
        recurring.setFrequency(request.getFrequency());
        recurring.setStartDate(request.getStartDate());
        recurring.setNextRunDate(request.getStartDate());
        recurring.setEndDate(request.getEndDate());
        recurring.setActive(request.getActive() == null || request.getActive());
        recurring.setIdempotencyPrefix(UUID.randomUUID().toString());

        return toDto(recurringTransactionRepository.save(recurring));
    }

    @Transactional
    public RecurringTransactionDto update(Long id, CreateRecurringTransactionRequest request) {
        User user = userService.getCurrentUser();
        validateRequest(request);

        RecurringTransaction recurring = recurringTransactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));

        Account sourceAccount = getOwnedAccount(user.getId(), request.getAccountId());
        Account destinationAccount = resolveDestinationAccount(user.getId(), request);
        Category category = resolveCategory(user.getId(), request);

        recurring.setAccount(sourceAccount);
        recurring.setDestinationAccount(destinationAccount);
        recurring.setCategory(category);
        recurring.setType(request.getType());
        recurring.setAmount(request.getAmount());
        recurring.setDescription(request.getDescription());
        recurring.setMerchant(request.getMerchant());
        recurring.setFrequency(request.getFrequency());
        recurring.setStartDate(request.getStartDate());
        recurring.setEndDate(request.getEndDate());
        if (recurring.getLastRunDate() == null || recurring.getNextRunDate().isBefore(request.getStartDate())) {
            recurring.setNextRunDate(request.getStartDate());
        }
        if (request.getActive() != null) {
            recurring.setActive(request.getActive());
        }
        if (recurring.getEndDate() != null && recurring.getNextRunDate().isAfter(recurring.getEndDate())) {
            recurring.setActive(false);
        }

        return toDto(recurringTransactionRepository.save(recurring));
    }

    @Transactional
    public void delete(Long id) {
        User user = userService.getCurrentUser();
        RecurringTransaction recurring = recurringTransactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));
        recurringTransactionRepository.delete(recurring);
    }

    @Transactional
    public void processDueRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions = recurringTransactionRepository
                .findAllByActiveTrueAndNextRunDateLessThanEqual(today);

        int processed = 0;
        for (RecurringTransaction recurring : dueTransactions) {
            processed += processRecurringTransaction(recurring, today);
        }

        if (processed > 0) {
            log.info("Auto-posted {} recurring transaction(s)", processed);
        }
    }

    private int processRecurringTransaction(RecurringTransaction recurring, LocalDate today) {
        int posted = 0;

        while (recurring.isActive()
                && !recurring.getNextRunDate().isAfter(today)
                && (recurring.getEndDate() == null || !recurring.getNextRunDate().isAfter(recurring.getEndDate()))) {
            LocalDate runDate = recurring.getNextRunDate();
            try {
                CreateTransactionRequest txRequest = new CreateTransactionRequest();
                txRequest.setAccountId(recurring.getAccount().getId());
                txRequest.setDestinationAccountId(
                        recurring.getDestinationAccount() != null ? recurring.getDestinationAccount().getId() : null);
                txRequest.setCategoryId(recurring.getCategory() != null ? recurring.getCategory().getId() : null);
                txRequest.setType(recurring.getType());
                txRequest.setAmount(recurring.getAmount());
                txRequest.setDescription(recurring.getDescription());
                txRequest.setMerchant(recurring.getMerchant());
                txRequest.setTransactionDate(runDate);
                if (recurring.getType() == TransactionType.TRANSFER) {
                    txRequest.setIdempotencyKey(recurring.getIdempotencyPrefix() + ":" + runDate);
                }

                transactionService.createTransactionForUser(recurring.getUser(), txRequest);
                recurring.setLastRunDate(runDate);
                recurring.setNextRunDate(nextRunDate(runDate, recurring.getFrequency()));
                posted++;
            } catch (Exception ex) {
                log.error("Failed processing recurring transaction id {} for run date {}",
                        recurring.getId(), runDate, ex);
                break;
            }
        }

        if (recurring.getEndDate() != null && recurring.getNextRunDate().isAfter(recurring.getEndDate())) {
            recurring.setActive(false);
        }

        recurringTransactionRepository.save(recurring);
        return posted;
    }

    private RecurringTransactionDto toDto(RecurringTransaction recurring) {
        return RecurringTransactionDto.builder()
                .id(recurring.getId())
                .accountId(recurring.getAccount().getId())
                .accountName(recurring.getAccount().getName())
                .destinationAccountId(recurring.getDestinationAccount() != null ? recurring.getDestinationAccount().getId() : null)
                .destinationAccountName(recurring.getDestinationAccount() != null ? recurring.getDestinationAccount().getName() : null)
                .category(recurring.getCategory() != null ? categoryMapper.toDto(recurring.getCategory()) : null)
                .type(recurring.getType())
                .amount(recurring.getAmount())
                .description(recurring.getDescription())
                .merchant(recurring.getMerchant())
                .frequency(recurring.getFrequency())
                .startDate(recurring.getStartDate())
                .nextRunDate(recurring.getNextRunDate())
                .endDate(recurring.getEndDate())
                .lastRunDate(recurring.getLastRunDate())
                .active(recurring.isActive())
                .createdAt(recurring.getCreatedAt())
                .build();
    }

    private Account getOwnedAccount(Long userId, Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    private Account resolveDestinationAccount(Long userId, CreateRecurringTransactionRequest request) {
        if (request.getType() != TransactionType.TRANSFER) {
            return null;
        }
        if (request.getDestinationAccountId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "destinationAccountId is required for TRANSFER");
        }
        if (Objects.equals(request.getAccountId(), request.getDestinationAccountId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Source and destination accounts must be different");
        }
        return getOwnedAccount(userId, request.getDestinationAccountId());
    }

    private Category resolveCategory(Long userId, CreateRecurringTransactionRequest request) {
        if (request.getType() == TransactionType.TRANSFER || request.getCategoryId() == null) {
            return null;
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        boolean allowed = category.isDefault()
                || (category.getUser() != null && Objects.equals(category.getUser().getId(), userId));
        if (!allowed) {
            throw new AccessDeniedException("Access denied");
        }
        return category;
    }

    private void validateRequest(CreateRecurringTransactionRequest request) {
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResponseStatusException(BAD_REQUEST, "endDate cannot be before startDate");
        }
    }

    private LocalDate nextRunDate(LocalDate current, RecurringFrequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
        };
    }
}
