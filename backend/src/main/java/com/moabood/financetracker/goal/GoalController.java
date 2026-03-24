package com.moabood.financetracker.goal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<List<GoalDto>> getAll() {
        return ResponseEntity.ok(goalService.getAll());
    }

    @PostMapping
    public ResponseEntity<GoalDto> create(@Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.ok(goalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        goalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
