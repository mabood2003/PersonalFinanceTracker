package com.moabood.financetracker.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String token;
    private String email;
    private String firstName;
    private String lastName;
}
