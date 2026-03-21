package com.moabood.financetracker.mapper;

import com.moabood.financetracker.category.Category;
import com.moabood.financetracker.category.CategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "isDefault", source = "default")
    CategoryDto toDto(Category category);
}
