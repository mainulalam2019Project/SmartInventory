package com.inventory.inventory.controller;

import com.inventory.inventory.dto.AdjustStockRequest;
import com.inventory.inventory.dto.StockCountRequest;
import com.inventory.inventory.model.Inventory;
import com.inventory.inventory.service.InventoryService;
import com.inventory.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService service;
    @GetMapping public ResponseEntity<ApiResponse<List<Inventory>>> list(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok(service.list(productId, warehouseId)));
    }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<Inventory>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.get(id)));
    }
    @PostMapping("/adjust") public ResponseEntity<ApiResponse<Inventory>> adjust(@Valid @RequestBody AdjustStockRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.adjust(req)));
    }
    @PostMapping("/count") public ResponseEntity<ApiResponse<Inventory>> count(@Valid @RequestBody StockCountRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(service.stockCount(req)));
    }
}
