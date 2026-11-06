package com.inventory.salesorder.controller;

import com.inventory.salesorder.model.SalesOrder;
import com.inventory.salesorder.service.SalesOrderService;
import com.inventory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {
    private final SalesOrderService service;
    @GetMapping public ResponseEntity<ApiResponse<List<SalesOrder>>> list() { return ResponseEntity.ok(ApiResponse.ok(service.findAll())); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<SalesOrder>> get(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.findById(id))); }
    @PostMapping public ResponseEntity<ApiResponse<SalesOrder>> create(@RequestBody SalesOrder o) { return ResponseEntity.status(201).body(ApiResponse.ok(service.create(o))); }
    @PostMapping("/{id}/confirm") public ResponseEntity<ApiResponse<SalesOrder>> confirm(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.confirm(id))); }
    @PostMapping("/{id}/fulfill") public ResponseEntity<ApiResponse<SalesOrder>> fulfill(@PathVariable Long id, @RequestParam(required = false) Long performedBy) { return ResponseEntity.ok(ApiResponse.ok(service.fulfill(id, performedBy))); }
    @PostMapping("/{id}/cancel") public ResponseEntity<ApiResponse<SalesOrder>> cancel(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.cancel(id))); }
}
