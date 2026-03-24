package com.moabood.financetracker.networth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/net-worth")
@RequiredArgsConstructor
public class NetWorthController {

    private final NetWorthService netWorthService;

    @GetMapping("/history")
    public ResponseEntity<List<NetWorthSnapshotDto>> getHistory(
            @RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(netWorthService.getHistory(days));
    }
}
