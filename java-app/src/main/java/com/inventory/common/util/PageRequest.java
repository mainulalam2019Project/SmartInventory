package com.inventory.common.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    private int page = 0;
    private int size = 20;
    private String sortBy = "id";
    private String sortDir = "asc";

    public int offset() { return page * size; }
}
