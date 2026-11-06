package com.inventory.transaction.controller;

import com.inventory.transaction.model.StockTransaction;
import com.inventory.transaction.service.TransactionService;
import com.inventory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService service;
    @GetMapping public ResponseEntity<ApiResponse<List<StockTransaction>>> list(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(service.findAll(productId, warehouseId, type, limit)));
    }
}
