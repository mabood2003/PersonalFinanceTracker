package com.moabood.financetracker.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabood.financetracker.common.PagedResponse;
import com.moabood.financetracker.common.ResourceNotFoundException;
import com.moabood.financetracker.config.JwtAuthenticationFilter;
import com.moabood.financetracker.config.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {JwtAuthenticationFilter.class}))
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TransactionService transactionService;
    @MockBean JwtService jwtService;

    @Test
    @WithMockUser
    void shouldReturn201_whenValidTransactionCreated() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(1L);
        request.setType(TransactionType.EXPENSE);
        request.setAmount(new BigDecimal("50.00"));
        request.setTransactionDate(LocalDate.now());

        TransactionDto dto = TransactionDto.builder()
                .id(1L).type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50.00"))
                .transactionDate(LocalDate.now()).build();

        when(transactionService.createTransaction(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void shouldReturn400_whenAmountIsNegative() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(1L);
        request.setType(TransactionType.EXPENSE);
        request.setAmount(new BigDecimal("-10.00"));
        request.setTransactionDate(LocalDate.now());

        mockMvc.perform(post("/api/v1/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400_whenRequiredFieldsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn404_whenTransactionNotFound() throws Exception {
        when(transactionService.getTransaction(99L))
                .thenThrow(new ResourceNotFoundException("Transaction not found with id: 99"));

        mockMvc.perform(get("/api/v1/transactions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction not found with id: 99"));
    }

    @Test
    @WithMockUser
    void shouldReturnPaginatedResults() throws Exception {
        TransactionDto dto = TransactionDto.builder().id(1L).build();
        PagedResponse<TransactionDto> paged = new PagedResponse<>(
                new PageImpl<>(List.of(dto)));

        when(transactionService.getTransactions(any(TransactionFilterRequest.class), any(Pageable.class)))
                .thenReturn(paged);

        mockMvc.perform(get("/api/v1/transactions?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
