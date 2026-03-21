package com.moabood.financetracker.mapper;

import com.moabood.financetracker.account.Account;
import com.moabood.financetracker.account.AccountDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountDto toDto(Account account);
}
