package com.inventory.salesorder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SalesOrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String sku;
    private int quantity;
    private BigDecimal unitPrice;
    private int quantityFulfilled;
}
