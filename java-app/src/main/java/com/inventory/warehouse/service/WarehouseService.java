package com.inventory.warehouse.service;

import com.inventory.warehouse.model.Warehouse;
import com.inventory.warehouse.repository.WarehouseRepository;
import com.inventory.common.exception.ConflictException;
import com.inventory.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository repo;
    public List<Warehouse> findAll() { return repo.findAll(); }
    public Warehouse findById(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Warehouse", id)); }
    @Transactional
    public Warehouse create(Warehouse w) {
        if (repo.existsByCode(w.getCode())) throw new ConflictException("Warehouse code already exists");
        if (w.getCapacity() == 0) w.setCapacity(0);
        return repo.save(w);
    }
    @Transactional
    public Warehouse update(Long id, Warehouse w) { findById(id); w.setId(id); return repo.save(w); }
    @Transactional
    public void delete(Long id) { findById(id); repo.delete(id); }
}
