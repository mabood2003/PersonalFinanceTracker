package com.moabood.financetracker.mapper;

import com.moabood.financetracker.transaction.Transaction;
import com.moabood.financetracker.transaction.TransactionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface TransactionMapper {

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountName", source = "account.name")
    @Mapping(target = "category", source = "category")
    TransactionDto toDto(Transaction transaction);
}
