package com.inventory.warehouse.controller;

import com.inventory.warehouse.model.Warehouse;
import com.inventory.warehouse.service.WarehouseService;
import com.inventory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService service;
    @GetMapping public ResponseEntity<ApiResponse<List<Warehouse>>> list() { return ResponseEntity.ok(ApiResponse.ok(service.findAll())); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<Warehouse>> get(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.findById(id))); }
    @PostMapping public ResponseEntity<ApiResponse<Warehouse>> create(@RequestBody Warehouse w) { return ResponseEntity.status(201).body(ApiResponse.ok(service.create(w))); }
    @PutMapping("/{id}") public ResponseEntity<ApiResponse<Warehouse>> update(@PathVariable Long id, @RequestBody Warehouse w) { return ResponseEntity.ok(ApiResponse.ok(service.update(id, w))); }
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.ok(ApiResponse.ok(null)); }
}
