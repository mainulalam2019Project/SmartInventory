package com.inventory.alert.service;

import com.inventory.alert.model.LowStockAlert;
import com.inventory.alert.repository.LowStockAlertRepository;
import com.inventory.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final LowStockAlertRepository repo;
    public List<LowStockAlert> findAll(String status) { return repo.findAll(status); }
    public LowStockAlert findById(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("LowStockAlert", id)); }
    @Transactional public void resolve(Long id) { findById(id); repo.resolve(id); }
}
