package com.inventory.salesorder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SalesOrder {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseCode;
    private String status;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private Long createdBy;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SalesOrderItem> items = new ArrayList<>();
}
