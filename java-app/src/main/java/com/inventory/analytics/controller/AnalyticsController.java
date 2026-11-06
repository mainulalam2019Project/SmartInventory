package com.inventory.analytics.controller;

import com.inventory.analytics.client.AnalyticsClient;
import com.inventory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsClient client;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        return ResponseEntity.ok(ApiResponse.ok(client.get("/analytics/v1/health")));
    }

    @PostMapping("/inventory/performance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> inventoryPerformance(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(client.post("/analytics/v1/inventory/performance", body)));
    }

    @PostMapping("/turnover")
    public ResponseEntity<ApiResponse<Map<String, Object>>> turnover(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(client.post("/analytics/v1/turnover", body)));
    }

    @PostMapping("/slow-moving")
    public ResponseEntity<ApiResponse<Map<String, Object>>> slowMoving(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(client.post("/analytics/v1/slow-moving", body)));
    }

    @PostMapping("/sales/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> monthlySales(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(client.post("/analytics/v1/sales/monthly", body)));
    }

    @PostMapping("/reorder-levels")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reorderLevels(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(client.post("/analytics/v1/reorder-levels", body)));
    }

    @PostMapping("/shortage-risk")
    public ResponseEntity<ApiResponse<Map<String, Object>>> shortageRisk(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(client.post("/analytics/v1/shortage-risk", body)));
    }

    @PostMapping("/forecast")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forecast(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(client.post("/analytics/v1/forecast", body)));
    }

    @PostMapping("/reports/generate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateReport(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.ok(client.post("/analytics/v1/reports/generate", body)));
    }
}
