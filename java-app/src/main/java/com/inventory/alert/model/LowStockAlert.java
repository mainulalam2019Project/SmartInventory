package com.inventory.alert.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LowStockAlert {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private Long warehouseId;
    private String warehouseCode;
    private int currentQty;
    private int threshold;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
