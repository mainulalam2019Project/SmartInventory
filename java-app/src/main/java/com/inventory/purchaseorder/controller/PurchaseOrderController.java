package com.inventory.purchaseorder.controller;

import com.inventory.purchaseorder.model.PurchaseOrder;
import com.inventory.purchaseorder.service.PurchaseOrderService;
import com.inventory.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    private final PurchaseOrderService service;
    @GetMapping public ResponseEntity<ApiResponse<List<PurchaseOrder>>> list() { return ResponseEntity.ok(ApiResponse.ok(service.findAll())); }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<PurchaseOrder>> get(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.findById(id))); }
    @PostMapping public ResponseEntity<ApiResponse<PurchaseOrder>> create(@RequestBody PurchaseOrder po) { return ResponseEntity.status(201).body(ApiResponse.ok(service.create(po))); }
    @PostMapping("/{id}/approve") public ResponseEntity<ApiResponse<PurchaseOrder>> approve(@PathVariable Long id, @RequestParam Long approvedBy) { return ResponseEntity.ok(ApiResponse.ok(service.approve(id, approvedBy))); }
    @PostMapping("/{id}/receive") public ResponseEntity<ApiResponse<PurchaseOrder>> receive(@PathVariable Long id, @RequestParam(required = false) Long performedBy) { return ResponseEntity.ok(ApiResponse.ok(service.receive(id, performedBy))); }
    @PostMapping("/{id}/items/{itemId}/receive") public ResponseEntity<ApiResponse<PurchaseOrder>> receivePartial(@PathVariable Long id, @PathVariable Long itemId, @RequestParam int qty, @RequestParam(required = false) Long performedBy) { return ResponseEntity.ok(ApiResponse.ok(service.receivePartial(id, itemId, qty, performedBy))); }
    @PostMapping("/{id}/cancel") public ResponseEntity<ApiResponse<PurchaseOrder>> cancel(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.ok(service.cancel(id))); }
}
