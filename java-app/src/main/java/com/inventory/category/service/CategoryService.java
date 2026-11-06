package com.inventory.category.service;

import com.inventory.category.model.Category;
import com.inventory.category.repository.CategoryRepository;
import com.inventory.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repo;
    public List<Category> findAll() { return repo.findAll(); }
    public Category findById(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id)); }
    @Transactional
    public Category create(Category c) { return repo.save(c); }
    @Transactional
    public Category update(Long id, Category c) { c.setId(id); findById(id); return repo.save(c); }
    @Transactional
    public void delete(Long id) { findById(id); repo.delete(id); }
}
