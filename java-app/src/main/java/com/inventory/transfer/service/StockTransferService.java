package com.inventory.transfer.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.inventory.service.InventoryService;
import com.inventory.transfer.model.StockTransfer;
import com.inventory.transfer.model.StockTransferItem;
import com.inventory.transfer.repository.StockTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTransferService {
    private final StockTransferRepository repo;
    private final InventoryService inventoryService;

    public List<StockTransfer> findAll() { return repo.findAll(); }
    public StockTransfer findById(Long id) {
        StockTransfer t = repo.findById(id);
        if (t == null) throw new ResourceNotFoundException("StockTransfer", id);
        return t;
    }

    @Transactional
    public StockTransfer create(StockTransfer t) {
        if (t.getTransferNumber() == null || t.getTransferNumber().isBlank()) t.setTransferNumber("TR-" + System.currentTimeMillis());
        if (t.getStatus() == null) t.setStatus("REQUESTED");
        return repo.save(t);
    }

    @Transactional
    public StockTransfer approve(Long id, Long approvedBy) {
        StockTransfer t = findById(id);
        if (!"REQUESTED".equals(t.getStatus())) throw new BadRequestException("Transfer not in REQUESTED status");
        repo.updateStatus(id, "APPROVED", approvedBy, null, null);
        t.setStatus("APPROVED"); t.setApprovedBy(approvedBy);
        return t;
    }

    @Transactional
    public StockTransfer ship(Long id, Long performedBy) {
        StockTransfer t = findById(id);
        if (!"APPROVED".equals(t.getStatus()) && !"REQUESTED".equals(t.getStatus()))
            throw new BadRequestException("Transfer cannot be shipped in status: " + t.getStatus());
        for (StockTransferItem it : t.getItems()) {
            int qty = it.getQuantityRequested() - it.getQuantityShipped();
            if (qty <= 0) continue;
            inventoryService.shipTransfer(it.getProductId(), t.getFromWarehouseId(), t.getToWarehouseId(), qty, t.getId(), performedBy);
            repo.updateItemShipped(it.getId(), qty);
            it.setQuantityShipped(it.getQuantityRequested());
        }
        LocalDateTime now = LocalDateTime.now();
        repo.updateStatus(id, "SHIPPED", t.getApprovedBy(), now, null);
        t.setStatus("SHIPPED"); t.setShippedAt(now);
        return t;
    }

    @Transactional
    public StockTransfer receive(Long id, Long performedBy) {
        StockTransfer t = findById(id);
        if (!"SHIPPED".equals(t.getStatus())) throw new BadRequestException("Transfer not shipped");
        for (StockTransferItem it : t.getItems()) {
            int qty = it.getQuantityShipped() - it.getQuantityReceived();
            if (qty <= 0) continue;
            inventoryService.receiveTransfer(it.getProductId(), t.getToWarehouseId(), qty, t.getId(), performedBy);
            repo.updateItemReceived(it.getId(), qty);
            it.setQuantityReceived(it.getQuantityShipped());
        }
        LocalDateTime now = LocalDateTime.now();
        repo.updateStatus(id, "RECEIVED", t.getApprovedBy(), t.getShippedAt(), now);
        t.setStatus("RECEIVED"); t.setReceivedAt(now);
        return t;
    }

    @Transactional
    public StockTransfer cancel(Long id) {
        StockTransfer t = findById(id);
        if ("RECEIVED".equals(t.getStatus())) throw new BadRequestException("Cannot cancel received transfer");
        repo.updateStatus(id, "CANCELLED", t.getApprovedBy(), t.getShippedAt(), t.getReceivedAt());
        t.setStatus("CANCELLED");
        return t;
    }
}
