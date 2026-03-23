package com.moabood.financetracker.recurring;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ResponseEntity<List<RecurringTransactionDto>> getAll() {
        return ResponseEntity.ok(recurringTransactionService.getAll());
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionDto> create(@Valid @RequestBody CreateRecurringTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringTransactionService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateRecurringTransactionRequest request
    ) {
        return ResponseEntity.ok(recurringTransactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recurringTransactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
