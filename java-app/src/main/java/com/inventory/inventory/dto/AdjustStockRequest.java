package com.inventory.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdjustStockRequest {
    @NotNull private Long productId;
    @NotNull private Long warehouseId;
    @NotNull private Integer quantity; // delta, can be negative
    private String notes;
    private Long performedBy;
}
