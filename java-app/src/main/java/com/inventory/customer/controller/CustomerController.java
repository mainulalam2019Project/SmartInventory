package com.inventory.customer.controller;

import com.inventory.customer.model.Customer;
import com.inventory.customer.service.CustomerService;
import com.inventory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService service;
    @GetMapping public ResponseEntity<ApiResponse<List<Customer>>> list() { return ResponseEntity.ok(ApiResponse.ok(service.findAll())); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<Customer>> get(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.findById(id))); }
    @PostMapping public ResponseEntity<ApiResponse<Customer>> create(@RequestBody Customer c) { return ResponseEntity.status(201).body(ApiResponse.ok(service.create(c))); }
    @PutMapping("/{id}") public ResponseEntity<ApiResponse<Customer>> update(@PathVariable Long id, @RequestBody Customer c) { return ResponseEntity.ok(ApiResponse.ok(service.update(id, c))); }
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.ok(ApiResponse.ok(null)); }
}
