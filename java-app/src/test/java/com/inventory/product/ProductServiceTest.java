package com.inventory.product;

import com.inventory.common.exception.ConflictException;
import com.inventory.product.model.Product;
import com.inventory.product.repository.ProductRepository;
import com.inventory.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repo;

    @InjectMocks
    private ProductService service;

    @Test
    void createThrowsOnDuplicateSku() {
        Product p = new Product();
        p.setSku("SKU-1");
        when(repo.existsBySku("SKU-1")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.create(p));
        verify(repo, never()).save(any());
    }

    @Test
    void createSavesWhenSkuUnique() {
        Product p = new Product();
        p.setSku("SKU-2");
        p.setName("Test");
        when(repo.existsBySku("SKU-2")).thenReturn(false);
        when(repo.save(p)).thenReturn(p);
        Product result = service.create(p);
        assertEquals("SKU-2", result.getSku());
        verify(repo).save(p);
    }
}
