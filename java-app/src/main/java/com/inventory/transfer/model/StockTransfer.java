package com.inventory.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockTransfer {
    private Long id;
    private String transferNumber;
    private Long fromWarehouseId;
    private String fromWarehouseCode;
    private Long toWarehouseId;
    private String toWarehouseCode;
    private String status;
    private Long requestedBy;
    private Long approvedBy;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StockTransferItem> items = new ArrayList<>();
}
