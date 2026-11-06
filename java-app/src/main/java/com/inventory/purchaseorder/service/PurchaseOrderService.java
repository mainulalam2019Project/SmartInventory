package com.inventory.purchaseorder.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.inventory.service.InventoryService;
import com.inventory.purchaseorder.model.PurchaseOrder;
import com.inventory.purchaseorder.model.PurchaseOrderItem;
import com.inventory.purchaseorder.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepository repo;
    private final InventoryService inventoryService;

    public List<PurchaseOrder> findAll() { return repo.findAll(); }
    public PurchaseOrder findById(Long id) {
        PurchaseOrder po = repo.findById(id);
        if (po == null) throw new ResourceNotFoundException("PurchaseOrder", id);
        return po;
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrder po) {
        if (po.getPoNumber() == null || po.getPoNumber().isBlank()) po.setPoNumber("PO-" + System.currentTimeMillis());
        if (po.getOrderDate() == null) po.setOrderDate(LocalDate.now());
        if (po.getStatus() == null) po.setStatus("DRAFT");
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItem it : po.getItems()) {
            if (it.getUnitCost() == null) it.setUnitCost(BigDecimal.ZERO);
            total = total.add(it.getUnitCost().multiply(BigDecimal.valueOf(it.getQuantityOrdered())));
        }
        po.setTotalAmount(total);
        return repo.save(po);
    }

    @Transactional
    public PurchaseOrder approve(Long id, Long approvedBy) {
        PurchaseOrder po = findById(id);
        if (!"DRAFT".equals(po.getStatus()) && !"SUBMITTED".equals(po.getStatus()))
            throw new BadRequestException("PO cannot be approved in status: " + po.getStatus());
        repo.updateStatus(id, "APPROVED", approvedBy);
        po.setStatus("APPROVED"); po.setApprovedBy(approvedBy);
        return po;
    }

    @Transactional
    public PurchaseOrder receive(Long id, Long performedBy) {
        PurchaseOrder po = findById(id);
        if (!"APPROVED".equals(po.getStatus()) && !"PARTIALLY_RECEIVED".equals(po.getStatus()))
            throw new BadRequestException("PO cannot be received in status: " + po.getStatus());

        boolean allReceived = true;
        for (PurchaseOrderItem it : po.getItems()) {
            int remaining = it.getQuantityOrdered() - it.getQuantityReceived();
            if (remaining <= 0) continue;
            // receive full remaining
            inventoryService.receiveStock(it.getProductId(), po.getWarehouseId(), remaining, "PURCHASE_ORDER", po.getId(), performedBy);
            repo.updateItemReceived(it.getId(), remaining);
            it.setQuantityReceived(it.getQuantityOrdered());
            allReceived = allReceived && (it.getQuantityReceived() >= it.getQuantityOrdered());
        }
        // recompute
        boolean fully = po.getItems().stream().allMatch(i -> i.getQuantityReceived() >= i.getQuantityOrdered());
        String newStatus = fully ? "RECEIVED" : "PARTIALLY_RECEIVED";
        repo.updateStatus(id, newStatus, null);
        po.setStatus(newStatus);
        return po;
    }

    @Transactional
    public PurchaseOrder receivePartial(Long poId, Long itemId, int qty, Long performedBy) {
        PurchaseOrder po = findById(poId);
        PurchaseOrderItem it = repo.findItem(itemId);
        if (it == null || !it.getPoId().equals(poId)) throw new ResourceNotFoundException("PO item", itemId);
        int remaining = it.getQuantityOrdered() - it.getQuantityReceived();
        if (qty <= 0 || qty > remaining) throw new BadRequestException("Invalid receive quantity");
        inventoryService.receiveStock(it.getProductId(), po.getWarehouseId(), qty, "PURCHASE_ORDER", po.getId(), performedBy);
        repo.updateItemReceived(itemId, qty);
        boolean fully = po.getItems().stream().allMatch(i -> (i.getId().equals(itemId) ? i.getQuantityReceived() + qty : i.getQuantityReceived()) >= i.getQuantityOrdered());
        repo.updateStatus(poId, fully ? "RECEIVED" : "PARTIALLY_RECEIVED", null);
        return findById(poId);
    }

    @Transactional
    public PurchaseOrder cancel(Long id) {
        PurchaseOrder po = findById(id);
        if ("RECEIVED".equals(po.getStatus())) throw new BadRequestException("Cannot cancel received PO");
        repo.updateStatus(id, "CANCELLED", null);
        po.setStatus("CANCELLED");
        return po;
    }
}
