package com.inventory.warehouse.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Warehouse {
    private Long id;
    private String code;
    private String name;
    private String address;
    private String city;
    private String country;
    private int capacity;
    private boolean active;
}
