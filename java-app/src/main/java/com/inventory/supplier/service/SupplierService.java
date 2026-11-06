package com.inventory.supplier.service;

import com.inventory.supplier.model.Supplier;
import com.inventory.supplier.repository.SupplierRepository;
import com.inventory.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository repo;
    public List<Supplier> findAll() { return repo.findAll(); }
    public Supplier findById(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier", id)); }
    @Transactional public Supplier create(Supplier s) { return repo.save(s); }
    @Transactional public Supplier update(Long id, Supplier s) { findById(id); s.setId(id); return repo.save(s); }
    @Transactional public void delete(Long id) { findById(id); repo.delete(id); }
}
