package com.moabood.financetracker.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabood.financetracker.config.JwtAuthenticationFilter;
import com.moabood.financetracker.config.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class}))
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean JwtService jwtService;

    @Test
    void shouldReturn201_whenValidRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("mo@example.com");
        request.setPassword("Password123!");
        request.setFirstName("Mo");
        request.setLastName("Abood");

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token").email("mo@example.com")
                .firstName("Mo").lastName("Abood").build();

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void shouldReturn409_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("mo@example.com");
        request.setPassword("Password123!");
        request.setFirstName("Mo");
        request.setLastName("Abood");

        when(authService.register(any())).thenThrow(new DataIntegrityViolationException("Email exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn200_withToken_whenValidLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("mo@example.com");
        request.setPassword("Password123!");

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token").email("mo@example.com")
                .firstName("Mo").lastName("Abood").build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldReturn401_whenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("mo@example.com");
        request.setPassword("wrongpassword");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
