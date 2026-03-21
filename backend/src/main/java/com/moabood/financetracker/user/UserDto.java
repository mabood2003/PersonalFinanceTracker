package com.moabood.financetracker.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
}
