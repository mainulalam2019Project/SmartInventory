package com.inventory.category.controller;

import com.inventory.category.model.Category;
import com.inventory.category.service.CategoryService;
import com.inventory.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;
    @GetMapping public ResponseEntity<ApiResponse<List<Category>>> list() { return ResponseEntity.ok(ApiResponse.ok(service.findAll())); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<Category>> get(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.findById(id))); }
    @PostMapping public ResponseEntity<ApiResponse<Category>> create(@Valid @RequestBody Category c) { return ResponseEntity.status(201).body(ApiResponse.ok(service.create(c))); }
    @PutMapping("/{id}") public ResponseEntity<ApiResponse<Category>> update(@PathVariable Long id, @Valid @RequestBody Category c) { return ResponseEntity.ok(ApiResponse.ok(service.update(id, c))); }
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.ok(ApiResponse.ok(null)); }
}
