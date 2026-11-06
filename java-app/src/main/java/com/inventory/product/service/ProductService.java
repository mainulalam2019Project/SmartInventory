package com.inventory.product.service;

import com.inventory.product.model.Product;
import com.inventory.product.repository.ProductRepository;
import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.ConflictException;
import com.inventory.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;
    public List<Product> search(String search, Long categoryId, String status) { return repo.findAll(search, categoryId, status); }
    public Product findById(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id)); }
    @Transactional
    public Product create(Product p) {
        if (repo.existsBySku(p.getSku())) throw new ConflictException("SKU already exists: " + p.getSku());
        if (p.getStatus() == null) p.setStatus("ACTIVE");
        if (p.getUnit() == null) p.setUnit("pcs");
        return repo.save(p);
    }
    @Transactional
    public Product update(Long id, Product p) {
        Product existing = findById(id);
        if (!existing.getSku().equals(p.getSku()) && repo.existsBySku(p.getSku()))
            throw new ConflictException("SKU already exists: " + p.getSku());
        p.setId(id);
        return repo.save(p);
    }
    @Transactional
    public void delete(Long id) { findById(id); repo.delete(id); }
}
