package com.inventory.product.controller;

import com.inventory.product.model.Product;
import com.inventory.product.service.ProductService;
import com.inventory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;
    @GetMapping public ResponseEntity<ApiResponse<List<Product>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(service.search(search, categoryId, status)));
    }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<Product>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }
    @PostMapping public ResponseEntity<ApiResponse<Product>> create(@RequestBody Product p) {
        return ResponseEntity.status(201).body(ApiResponse.ok(service.create(p)));
    }
    @PutMapping("/{id}") public ResponseEntity<ApiResponse<Product>> update(@PathVariable Long id, @RequestBody Product p) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, p)));
    }
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id); return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
