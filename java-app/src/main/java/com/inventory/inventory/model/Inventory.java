package com.inventory.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Inventory {
    private Long id;
    private Long productId;
    private String productName;
    private String sku;
    private Long warehouseId;
    private String warehouseCode;
    private int quantityOnHand;
    private int quantityReserved;
    private int quantityInTransit;
    private int version;
    private LocalDateTime lastCountedAt;
    private LocalDateTime updatedAt;

    public int getAvailable() { return quantityOnHand - quantityReserved; }
}
