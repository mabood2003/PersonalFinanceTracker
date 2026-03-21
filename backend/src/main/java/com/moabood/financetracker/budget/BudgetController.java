package com.moabood.financetracker.budget;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetProgressDto>> getAll() {
        return ResponseEntity.ok(budgetService.getAllBudgetsWithProgress());
    }

    @PostMapping
    public ResponseEntity<BudgetDto> create(@Valid @RequestBody CreateBudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.createBudget(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> update(@PathVariable Long id, @Valid @RequestBody CreateBudgetRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
