package com.inventory.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockTransaction {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private Long warehouseId;
    private String warehouseCode;
    private String type;
    private int quantity;
    private String referenceType;
    private Long referenceId;
    private Long performedBy;
    private LocalDateTime occurredAt;
    private String notes;
}
