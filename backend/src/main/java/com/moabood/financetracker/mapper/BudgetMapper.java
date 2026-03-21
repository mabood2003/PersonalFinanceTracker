package com.moabood.financetracker.mapper;

import com.moabood.financetracker.budget.Budget;
import com.moabood.financetracker.budget.BudgetDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface BudgetMapper {
    BudgetDto toDto(Budget budget);
}
