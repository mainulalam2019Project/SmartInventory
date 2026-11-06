package com.inventory.salesorder.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.inventory.service.InventoryService;
import com.inventory.salesorder.model.SalesOrder;
import com.inventory.salesorder.model.SalesOrderItem;
import com.inventory.salesorder.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesOrderService {
    private final SalesOrderRepository repo;
    private final InventoryService inventoryService;

    public List<SalesOrder> findAll() { return repo.findAll(); }
    public SalesOrder findById(Long id) {
        SalesOrder o = repo.findById(id);
        if (o == null) throw new ResourceNotFoundException("SalesOrder", id);
        return o;
    }

    @Transactional
    public SalesOrder create(SalesOrder o) {
        if (o.getOrderNumber() == null || o.getOrderNumber().isBlank()) o.setOrderNumber("SO-" + System.currentTimeMillis());
        if (o.getOrderDate() == null) o.setOrderDate(LocalDate.now());
        if (o.getStatus() == null) o.setStatus("PENDING");
        BigDecimal total = BigDecimal.ZERO;
        for (SalesOrderItem it : o.getItems()) {
            if (it.getUnitPrice() == null) it.setUnitPrice(BigDecimal.ZERO);
            total = total.add(it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity())));
        }
        o.setTotalAmount(total);
        return repo.save(o);
    }

    @Transactional
    public SalesOrder confirm(Long id) {
        SalesOrder o = findById(id);
        if (!"PENDING".equals(o.getStatus())) throw new BadRequestException("Order not in PENDING status");
        for (SalesOrderItem it : o.getItems()) {
            boolean ok = inventoryService.tryReserve(it.getProductId(), o.getWarehouseId(), it.getQuantity());
            if (!ok) throw new BadRequestException("Insufficient stock to reserve for product " + it.getProductId());
        }
        repo.updateStatus(id, "CONFIRMED");
        o.setStatus("CONFIRMED");
        return o;
    }

    @Transactional
    public SalesOrder fulfill(Long id, Long performedBy) {
        SalesOrder o = findById(id);
        if (!"CONFIRMED".equals(o.getStatus()) && !"ALLOCATED".equals(o.getStatus()))
            throw new BadRequestException("Order not ready for fulfillment: " + o.getStatus());
        for (SalesOrderItem it : o.getItems()) {
            int remaining = it.getQuantity() - it.getQuantityFulfilled();
            if (remaining <= 0) continue;
            inventoryService.releaseReserved(it.getProductId(), o.getWarehouseId(), remaining);
            inventoryService.issueStock(it.getProductId(), o.getWarehouseId(), remaining, "SALES_ORDER", o.getId(), performedBy);
            repo.updateItemFulfilled(it.getId(), remaining);
            it.setQuantityFulfilled(it.getQuantity());
        }
        repo.updateStatus(id, "SHIPPED");
        o.setStatus("SHIPPED");
        return o;
    }

    @Transactional
    public SalesOrder cancel(Long id) {
        SalesOrder o = findById(id);
        if ("SHIPPED".equals(o.getStatus()) || "DELIVERED".equals(o.getStatus()))
            throw new BadRequestException("Cannot cancel shipped/delivered order");
        // release reservations if confirmed
        if ("CONFIRMED".equals(o.getStatus()) || "ALLOCATED".equals(o.getStatus())) {
            for (SalesOrderItem it : o.getItems()) {
                inventoryService.releaseReserved(it.getProductId(), o.getWarehouseId(), it.getQuantity() - it.getQuantityFulfilled());
            }
        }
        repo.updateStatus(id, "CANCELLED");
        o.setStatus("CANCELLED");
        return o;
    }
}
