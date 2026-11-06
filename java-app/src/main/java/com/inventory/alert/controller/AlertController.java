package com.inventory.alert.controller;

import com.inventory.alert.model.LowStockAlert;
import com.inventory.alert.service.AlertService;
import com.inventory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final AlertService service;
    @GetMapping public ResponseEntity<ApiResponse<List<LowStockAlert>>> list(@RequestParam(required = false) String status) { return ResponseEntity.ok(ApiResponse.ok(service.findAll(status))); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<LowStockAlert>> get(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.findById(id))); }
    @PostMapping("/{id}/resolve") public ResponseEntity<ApiResponse<Void>> resolve(@PathVariable Long id) { service.resolve(id); return ResponseEntity.ok(ApiResponse.ok(null)); }
}
