package com.inventory.inventory.service;

import com.inventory.common.exception.BadRequestException;
import com.inventory.common.exception.ConflictException;
import com.inventory.common.exception.ResourceNotFoundException;
import com.inventory.inventory.dto.AdjustStockRequest;
import com.inventory.inventory.dto.StockCountRequest;
import com.inventory.inventory.model.Inventory;
import com.inventory.inventory.repository.InventoryRepository;
import com.inventory.product.model.Product;
import com.inventory.product.repository.ProductRepository;
import com.inventory.transaction.model.StockTransaction;
import com.inventory.transaction.repository.StockTransactionRepository;
import com.inventory.alert.model.LowStockAlert;
import com.inventory.alert.repository.LowStockAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepo;
    private final ProductRepository productRepo;
    private final StockTransactionRepository txnRepo;
    private final LowStockAlertRepository alertRepo;

    public List<Inventory> list(Long productId, Long warehouseId) {
        return inventoryRepo.findAll(productId, warehouseId);
    }

    public Inventory get(Long id) {
        return inventoryRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
    }

    @Transactional
    public Inventory adjust(AdjustStockRequest req) {
        if (req.getQuantity() == null || req.getQuantity() == 0)
            throw new BadRequestException("Quantity must be non-zero");
        Inventory inv = getOrCreate(req.getProductId(), req.getWarehouseId());
        int expectedVersion = inv.getVersion();
        boolean ok = inventoryRepo.adjust(inv.getId(), req.getQuantity(), expectedVersion);
        if (!ok) throw new ConflictException("Concurrent update on inventory row; retry");

        StockTransaction t = new StockTransaction();
        t.setProductId(req.getProductId());
        t.setWarehouseId(req.getWarehouseId());
        t.setType(req.getQuantity() > 0 ? "RECEIPT" : "ISSUE");
        t.setQuantity(req.getQuantity());
        t.setReferenceType("MANUAL");
        t.setPerformedBy(req.getPerformedBy());
        t.setNotes(req.getNotes());
        txnRepo.save(t);

        Inventory updated = inventoryRepo.findById(inv.getId()).orElseThrow();
        checkLowStock(updated);
        return updated;
    }

    @Transactional
    public Inventory stockCount(StockCountRequest req) {
        Inventory inv = getOrCreate(req.getProductId(), req.getWarehouseId());
        int delta = req.getCountedQuantity() - inv.getQuantityOnHand();
        boolean ok = inventoryRepo.adjust(inv.getId(), delta, inv.getVersion());
        if (!ok) throw new ConflictException("Concurrent update on inventory row; retry");

        StockTransaction t = new StockTransaction();
        t.setProductId(req.getProductId());
        t.setWarehouseId(req.getWarehouseId());
        t.setType("COUNT");
        t.setQuantity(delta);
        t.setReferenceType("STOCK_COUNT");
        t.setPerformedBy(req.getPerformedBy());
        t.setNotes(req.getNotes() == null ? "Cycle count" : req.getNotes());
        txnRepo.save(t);

        Inventory updated = inventoryRepo.findById(inv.getId()).orElseThrow();
        checkLowStock(updated);
        return updated;
    }

    private Inventory getOrCreate(Long productId, Long warehouseId) {
        return inventoryRepo.findByProductAndWarehouse(productId, warehouseId).orElseGet(() -> {
            Inventory i = new Inventory();
            i.setProductId(productId);
            i.setWarehouseId(warehouseId);
            i.setQuantityOnHand(0);
            i.setQuantityReserved(0);
            i.setQuantityInTransit(0);
            return inventoryRepo.save(i);
        });
    }

    private void checkLowStock(Inventory inv) {
        Product p = productRepo.findById(inv.getProductId()).orElse(null);
        if (p == null) return;
        if (inv.getQuantityOnHand() <= p.getReorderPoint() && !alertRepo.existsOpen(inv.getProductId(), inv.getWarehouseId())) {
            LowStockAlert a = new LowStockAlert();
            a.setProductId(inv.getProductId());
            a.setWarehouseId(inv.getWarehouseId());
            a.setCurrentQty(inv.getQuantityOnHand());
            a.setThreshold(p.getReorderPoint());
            a.setStatus("OPEN");
            alertRepo.save(a);
        }
    }

    // Internal helpers used by order services
    @Transactional
    public boolean tryReserve(Long productId, Long warehouseId, int qty) {
        Inventory inv = inventoryRepo.findByProductAndWarehouse(productId, warehouseId).orElse(null);
        if (inv == null) return false;
        return inventoryRepo.reserve(inv.getId(), qty, inv.getVersion());
    }

    @Transactional
    public void releaseReserved(Long productId, Long warehouseId, int qty) {
        Inventory inv = inventoryRepo.findByProductAndWarehouse(productId, warehouseId).orElse(null);
        if (inv != null) inventoryRepo.releaseReservation(inv.getId(), qty);
    }

    @Transactional
    public void issueStock(Long productId, Long warehouseId, int qty, String refType, Long refId, Long performedBy) {
        Inventory inv = inventoryRepo.findByProductAndWarehouse(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", productId));
        boolean ok = inventoryRepo.adjust(inv.getId(), -qty, inv.getVersion());
        if (!ok) throw new ConflictException("Concurrent update on inventory row; retry");

        StockTransaction t = new StockTransaction();
        t.setProductId(productId);
        t.setWarehouseId(warehouseId);
        t.setType("ISSUE");
        t.setQuantity(-qty);
        t.setReferenceType(refType);
        t.setReferenceId(refId);
        t.setPerformedBy(performedBy);
        txnRepo.save(t);
    }

    @Transactional
    public void shipTransfer(Long productId, Long fromWh, Long toWh, int qty, Long transferId, Long performedBy) {
        Inventory inv = inventoryRepo.findByProductAndWarehouse(productId, fromWh)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", productId));
        boolean ok = inventoryRepo.adjust(inv.getId(), -qty, inv.getVersion());
        if (!ok) throw new ConflictException("Concurrent update on inventory row; retry");
        inventoryRepo.addInTransit(productId, toWh, qty);

        StockTransaction t = new StockTransaction();
        t.setProductId(productId); t.setWarehouseId(fromWh); t.setType("TRANSFER_OUT"); t.setQuantity(-qty);
        t.setReferenceType("STOCK_TRANSFER"); t.setReferenceId(transferId); t.setPerformedBy(performedBy);
        txnRepo.save(t);
    }

    @Transactional
    public void receiveTransfer(Long productId, Long toWh, int qty, Long transferId, Long performedBy) {
        inventoryRepo.convertInTransitToOnHand(productId, toWh, qty);
        StockTransaction t = new StockTransaction();
        t.setProductId(productId); t.setWarehouseId(toWh); t.setType("TRANSFER_IN"); t.setQuantity(qty);
        t.setReferenceType("STOCK_TRANSFER"); t.setReferenceId(transferId); t.setPerformedBy(performedBy);
        txnRepo.save(t);
    }

    @Transactional
    public void receiveStock(Long productId, Long warehouseId, int qty, String refType, Long refId, Long performedBy) {
        Inventory inv = getOrCreate(productId, warehouseId);
        boolean ok = inventoryRepo.adjust(inv.getId(), qty, inv.getVersion());
        if (!ok) throw new ConflictException("Concurrent update on inventory row; retry");

        StockTransaction t = new StockTransaction();
        t.setProductId(productId);
        t.setWarehouseId(warehouseId);
        t.setType("RECEIPT");
        t.setQuantity(qty);
        t.setReferenceType(refType);
        t.setReferenceId(refId);
        t.setPerformedBy(performedBy);
        txnRepo.save(t);
    }
}
