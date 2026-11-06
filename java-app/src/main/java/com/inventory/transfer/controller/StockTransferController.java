package com.inventory.transfer.controller;

import com.inventory.transfer.model.StockTransfer;
import com.inventory.transfer.service.StockTransferService;
import com.inventory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class StockTransferController {
    private final StockTransferService service;
    @GetMapping public ResponseEntity<ApiResponse<List<StockTransfer>>> list() { return ResponseEntity.ok(ApiResponse.ok(service.findAll())); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<StockTransfer>> get(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.findById(id))); }
    @PostMapping public ResponseEntity<ApiResponse<StockTransfer>> create(@RequestBody StockTransfer t) { return ResponseEntity.status(201).body(ApiResponse.ok(service.create(t))); }
    @PostMapping("/{id}/approve") public ResponseEntity<ApiResponse<StockTransfer>> approve(@PathVariable Long id, @RequestParam Long approvedBy) { return ResponseEntity.ok(ApiResponse.ok(service.approve(id, approvedBy))); }
    @PostMapping("/{id}/ship") public ResponseEntity<ApiResponse<StockTransfer>> ship(@PathVariable Long id, @RequestParam(required = false) Long performedBy) { return ResponseEntity.ok(ApiResponse.ok(service.ship(id, performedBy))); }
    @PostMapping("/{id}/receive") public ResponseEntity<ApiResponse<StockTransfer>> receive(@PathVariable Long id, @RequestParam(required = false) Long performedBy) { return ResponseEntity.ok(ApiResponse.ok(service.receive(id, performedBy))); }
    @PostMapping("/{id}/cancel") public ResponseEntity<ApiResponse<StockTransfer>> cancel(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.cancel(id))); }
}
