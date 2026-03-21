package com.moabood.financetracker.analytics;

import com.moabood.financetracker.transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryDto> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(analyticsService.getMonthlySummary(year, month));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownDto>> getCategoryBreakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "EXPENSE") TransactionType type) {
        return ResponseEntity.ok(analyticsService.getCategoryBreakdown(startDate, endDate, type));
    }

    @GetMapping("/spending-trend")
    public ResponseEntity<List<SpendingTrendDto>> getSpendingTrend(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(analyticsService.getSpendingTrend(months));
    }
}
