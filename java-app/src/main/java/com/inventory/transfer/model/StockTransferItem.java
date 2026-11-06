package com.inventory.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockTransferItem {
    private Long id;
    private Long transferId;
    private Long productId;
    private String productName;
    private String sku;
    private int quantityRequested;
    private int quantityShipped;
    private int quantityReceived;
}
