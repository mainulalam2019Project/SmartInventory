package com.inventory.purchaseorder.repository;

import com.inventory.purchaseorder.model.PurchaseOrder;
import com.inventory.purchaseorder.model.PurchaseOrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PurchaseOrderRepository {
    private final JdbcTemplate jdbc;

    private static final RowMapper<PurchaseOrder> PO_MAPPER = (rs, n) -> {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(rs.getLong("id"));
        po.setPoNumber(rs.getString("po_number"));
        po.setSupplierId(rs.getLong("supplier_id"));
        po.setSupplierName(rs.getString("supplier_name"));
        po.setWarehouseId(rs.getLong("warehouse_id"));
        po.setWarehouseCode(rs.getString("warehouse_code"));
        po.setStatus(rs.getString("status"));
        String od = rs.getString("order_date"); if (od != null) po.setOrderDate(java.time.LocalDate.parse(od.substring(0, 10)));
        String ed = rs.getString("expected_date"); if (ed != null) po.setExpectedDate(java.time.LocalDate.parse(ed.substring(0, 10)));
        po.setTotalAmount(rs.getBigDecimal("total_amount"));
        long cb = rs.getLong("created_by"); if (!rs.wasNull()) po.setCreatedBy(cb);
        long ab = rs.getLong("approved_by"); if (!rs.wasNull()) po.setApprovedBy(ab);
        po.setNotes(rs.getString("notes"));
        Timestamp c = rs.getTimestamp("created_at"); if (c != null) po.setCreatedAt(c.toLocalDateTime());
        Timestamp u = rs.getTimestamp("updated_at"); if (u != null) po.setUpdatedAt(u.toLocalDateTime());
        return po;
    };

    private static final RowMapper<PurchaseOrderItem> ITEM_MAPPER = (rs, n) -> {
        PurchaseOrderItem it = new PurchaseOrderItem();
        it.setId(rs.getLong("id"));
        it.setPoId(rs.getLong("po_id"));
        it.setProductId(rs.getLong("product_id"));
        it.setProductName(rs.getString("product_name"));
        it.setSku(rs.getString("sku"));
        it.setQuantityOrdered(rs.getInt("quantity_ordered"));
        it.setQuantityReceived(rs.getInt("quantity_received"));
        it.setUnitCost(rs.getBigDecimal("unit_cost"));
        return it;
    };

    private static final String PO_BASE = """
        SELECT po.*, s.name AS supplier_name, w.code AS warehouse_code
        FROM purchase_orders po JOIN suppliers s ON po.supplier_id = s.id JOIN warehouses w ON po.warehouse_id = w.id
        """;

    public List<PurchaseOrder> findAll() {
        List<PurchaseOrder> pos = jdbc.query(PO_BASE + " ORDER BY po.id DESC", PO_MAPPER);
        for (PurchaseOrder po : pos) po.setItems(findItems(po.getId()));
        return pos;
    }

    public PurchaseOrder findById(Long id) {
        List<PurchaseOrder> pos = jdbc.query(PO_BASE + " WHERE po.id = ?", PO_MAPPER, id);
        if (pos.isEmpty()) return null;
        PurchaseOrder po = pos.get(0);
        po.setItems(findItems(id));
        return po;
    }

    private List<PurchaseOrderItem> findItems(Long poId) {
        return jdbc.query("""
            SELECT poi.*, p.name AS product_name, p.sku AS sku
            FROM purchase_order_items poi JOIN products p ON poi.product_id = p.id
            WHERE poi.po_id = ?""", ITEM_MAPPER, poId);
    }

    public PurchaseOrder save(PurchaseOrder po) {
        if (po.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO purchase_orders (po_number,supplier_id,warehouse_id,status,order_date,expected_date,total_amount,created_by,notes) VALUES (?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, po.getPoNumber()); ps.setLong(2, po.getSupplierId()); ps.setLong(3, po.getWarehouseId());
                ps.setString(4, po.getStatus() == null ? "DRAFT" : po.getStatus());
                ps.setString(5, po.getOrderDate().toString());
                if (po.getExpectedDate() == null) ps.setNull(6, java.sql.Types.VARCHAR); else ps.setString(6, po.getExpectedDate().toString());
                ps.setBigDecimal(7, po.getTotalAmount() == null ? BigDecimal.ZERO : po.getTotalAmount());
                if (po.getCreatedBy() == null) ps.setNull(8, java.sql.Types.BIGINT); else ps.setLong(8, po.getCreatedBy());
                ps.setString(9, po.getNotes());
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) po.setId(k.longValue());

            for (PurchaseOrderItem it : po.getItems()) {
                jdbc.update("INSERT INTO purchase_order_items (po_id,product_id,quantity_ordered,quantity_received,unit_cost) VALUES (?,?,?,?,?)",
                    po.getId(), it.getProductId(), it.getQuantityOrdered(), 0, it.getUnitCost());
            }
        } else {
            jdbc.update("UPDATE purchase_orders SET status=?, expected_date=?, notes=?, updated_at=CURRENT_TIMESTAMP WHERE id=?",
                po.getStatus(), po.getExpectedDate(), po.getNotes(), po.getId());
        }
        return po;
    }

    public void updateStatus(Long id, String status, Long approvedBy) {
        if (approvedBy != null) {
            jdbc.update("UPDATE purchase_orders SET status=?, approved_by=?, updated_at=CURRENT_TIMESTAMP WHERE id=?", status, approvedBy, id);
        } else {
            jdbc.update("UPDATE purchase_orders SET status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?", status, id);
        }
    }

    public void updateItemReceived(Long itemId, int quantityReceived) {
        jdbc.update("UPDATE purchase_order_items SET quantity_received = quantity_received + ? WHERE id = ?", quantityReceived, itemId);
    }

    public PurchaseOrderItem findItem(Long itemId) {
        List<PurchaseOrderItem> l = jdbc.query("""
            SELECT poi.*, p.name AS product_name, p.sku AS sku
            FROM purchase_order_items poi JOIN products p ON poi.product_id = p.id WHERE poi.id = ?""", ITEM_MAPPER, itemId);
        return l.isEmpty() ? null : l.get(0);
    }
}
