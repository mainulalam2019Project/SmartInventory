package com.inventory.customer.service;

import com.inventory.customer.model.Customer;
import com.inventory.customer.repository.CustomerRepository;
import com.inventory.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repo;
    public List<Customer> findAll() { return repo.findAll(); }
    public Customer findById(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", id)); }
    @Transactional public Customer create(Customer c) { return repo.save(c); }
    @Transactional public Customer update(Long id, Customer c) { findById(id); c.setId(id); return repo.save(c); }
    @Transactional public void delete(Long id) { findById(id); repo.delete(id); }
}
