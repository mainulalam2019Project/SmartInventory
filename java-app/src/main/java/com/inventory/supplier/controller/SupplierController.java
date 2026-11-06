package com.inventory.supplier.controller;

import com.inventory.supplier.model.Supplier;
import com.inventory.supplier.service.SupplierService;
import com.inventory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService service;
    @GetMapping public ResponseEntity<ApiResponse<List<Supplier>>> list() { return ResponseEntity.ok(ApiResponse.ok(service.findAll())); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<Supplier>> get(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.findById(id))); }
    @PostMapping public ResponseEntity<ApiResponse<Supplier>> create(@RequestBody Supplier s) { return ResponseEntity.status(201).body(ApiResponse.ok(service.create(s))); }
    @PutMapping("/{id}") public ResponseEntity<ApiResponse<Supplier>> update(@PathVariable Long id, @RequestBody Supplier s) { return ResponseEntity.ok(ApiResponse.ok(service.update(id, s))); }
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.ok(ApiResponse.ok(null)); }
}
