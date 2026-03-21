package com.moabood.financetracker.mapper;

import com.moabood.financetracker.user.User;
import com.moabood.financetracker.user.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
