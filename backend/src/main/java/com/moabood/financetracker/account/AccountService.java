package com.moabood.financetracker.account;

import com.moabood.financetracker.common.ResourceNotFoundException;
import com.moabood.financetracker.mapper.AccountMapper;
import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserService userService;

    public List<AccountDto> getAllAccounts() {
        User user = userService.getCurrentUser();
        return accountRepository.findAllByUserId(user.getId())
                .stream().map(accountMapper::toDto).toList();
    }

    public AccountDto getAccount(Long id) {
        User user = userService.getCurrentUser();
        Account account = accountRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return accountMapper.toDto(account);
    }

    @Transactional
    public AccountDto createAccount(CreateAccountRequest request) {
        User user = userService.getCurrentUser();
        Account account = new Account();
        account.setUser(user);
        account.setName(request.getName());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getBalance());
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : "CAD");
        return accountMapper.toDto(accountRepository.save(account));
    }

    @Transactional
    public AccountDto updateAccount(Long id, CreateAccountRequest request) {
        User user = userService.getCurrentUser();
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        account.setName(request.getName());
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getBalance());
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : "CAD");
        return accountMapper.toDto(accountRepository.save(account));
    }

    @Transactional
    public void deleteAccount(Long id) {
        User user = userService.getCurrentUser();
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        accountRepository.delete(account);
    }
}
