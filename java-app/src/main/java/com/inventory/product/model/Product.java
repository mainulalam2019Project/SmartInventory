package com.inventory.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Product {
    private Long id;
    private String sku;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String unit;
    private String barcode;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private String status;
    private int reorderPoint;
    private int safetyStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
