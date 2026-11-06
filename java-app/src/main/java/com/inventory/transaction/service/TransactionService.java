package com.inventory.transaction.service;

import com.inventory.transaction.model.StockTransaction;
import com.inventory.transaction.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final StockTransactionRepository repo;
    public List<StockTransaction> findAll(Long productId, Long warehouseId, String type, int limit) {
        return repo.findAll(productId, warehouseId, type, limit);
    }
}
