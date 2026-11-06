package com.inventory.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockCountRequest {
    @NotNull private Long productId;
    @NotNull private Long warehouseId;
    @NotNull private Integer countedQuantity;
    private String notes;
    private Long performedBy;
}
