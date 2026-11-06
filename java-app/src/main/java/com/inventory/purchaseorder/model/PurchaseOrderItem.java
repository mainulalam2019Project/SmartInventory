package com.inventory.purchaseorder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderItem {
    private Long id;
    private Long poId;
    private Long productId;
    private String productName;
    private String sku;
    private int quantityOrdered;
    private int quantityReceived;
    private BigDecimal unitCost;
}
