package com.inventory.inventory.repository;

import com.inventory.inventory.model.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryRepository {
    private final JdbcTemplate jdbc;

    private static final RowMapper<Inventory> M = (rs, n) -> {
        Inventory i = new Inventory();
        i.setId(rs.getLong("id"));
        i.setProductId(rs.getLong("product_id"));
        i.setProductName(rs.getString("product_name"));
        i.setSku(rs.getString("sku"));
        i.setWarehouseId(rs.getLong("warehouse_id"));
        i.setWarehouseCode(rs.getString("warehouse_code"));
        i.setQuantityOnHand(rs.getInt("quantity_on_hand"));
        i.setQuantityReserved(rs.getInt("quantity_reserved"));
        i.setQuantityInTransit(rs.getInt("quantity_in_transit"));
        i.setVersion(rs.getInt("version"));
        Timestamp lc = rs.getTimestamp("last_counted_at"); if (lc != null) i.setLastCountedAt(lc.toLocalDateTime());
        Timestamp up = rs.getTimestamp("updated_at"); if (up != null) i.setUpdatedAt(up.toLocalDateTime());
        return i;
    };

    private static final String BASE = """
        SELECT i.*, p.name AS product_name, p.sku AS sku, w.code AS warehouse_code
        FROM inventory i JOIN products p ON i.product_id = p.id JOIN warehouses w ON i.warehouse_id = w.id
        """;

    public List<Inventory> findAll(Long productId, Long warehouseId) {
        StringBuilder sql = new StringBuilder(BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (productId != null) { sql.append(" AND i.product_id = ?"); params.add(productId); }
        if (warehouseId != null) { sql.append(" AND i.warehouse_id = ?"); params.add(warehouseId); }
        sql.append(" ORDER BY i.id");
        return jdbc.query(sql.toString(), M, params.toArray());
    }

    public Optional<Inventory> findById(Long id) {
        var l = jdbc.query(BASE + " WHERE i.id = ?", M, id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    public Optional<Inventory> findByProductAndWarehouse(Long productId, Long warehouseId) {
        var l = jdbc.query(BASE + " WHERE i.product_id = ? AND i.warehouse_id = ?", M, productId, warehouseId);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    public Inventory save(Inventory i) {
        if (i.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO inventory (product_id,warehouse_id,quantity_on_hand,quantity_reserved,quantity_in_transit,version) VALUES (?,?,?,?,?,0)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, i.getProductId()); ps.setLong(2, i.getWarehouseId());
                ps.setInt(3, i.getQuantityOnHand()); ps.setInt(4, i.getQuantityReserved()); ps.setInt(5, i.getQuantityInTransit());
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) i.setId(k.longValue());
        } else {
            jdbc.update("UPDATE inventory SET quantity_on_hand=?,quantity_reserved=?,quantity_in_transit=?,version=version+1,updated_at=CURRENT_TIMESTAMP WHERE id=?",
                i.getQuantityOnHand(), i.getQuantityReserved(), i.getQuantityInTransit(), i.getId());
        }
        return i;
    }

    /** Adjust on-hand by delta; returns false if insufficient stock or version conflict. */
    public boolean adjust(Long id, int delta, int expectedVersion) {
        int updated = jdbc.update(
            "UPDATE inventory SET quantity_on_hand = quantity_on_hand + ?, version = version + 1, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ? AND (quantity_on_hand + ?) >= 0",
            delta, id, expectedVersion, delta);
        return updated == 1;
    }

    public boolean reserve(Long id, int qty, int expectedVersion) {
        int updated = jdbc.update(
            "UPDATE inventory SET quantity_reserved = quantity_reserved + ?, version = version + 1, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ? AND (quantity_on_hand - quantity_reserved - ?) >= 0",
            qty, id, expectedVersion, qty);
        return updated == 1;
    }

    public void releaseReservation(Long id, int qty) {
        jdbc.update("UPDATE inventory SET quantity_reserved = quantity_reserved - ?, version = version + 1, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND quantity_reserved >= ?",
            qty, id, qty);
    }

    public void addInTransit(Long productId, Long warehouseId, int qty) {
        int updated = jdbc.update("UPDATE inventory SET quantity_in_transit = quantity_in_transit + ?, version = version + 1, updated_at = CURRENT_TIMESTAMP WHERE product_id = ? AND warehouse_id = ?", qty, productId, warehouseId);
        if (updated == 0) {
            // create row
            jdbc.update("INSERT INTO inventory (product_id,warehouse_id,quantity_on_hand,quantity_reserved,quantity_in_transit,version) VALUES (?,?,0,0,?,0)", productId, warehouseId, qty);
        }
    }

    public void convertInTransitToOnHand(Long productId, Long warehouseId, int qty) {
        jdbc.update("UPDATE inventory SET quantity_in_transit = quantity_in_transit - ?, quantity_on_hand = quantity_on_hand + ?, version = version + 1, updated_at = CURRENT_TIMESTAMP WHERE product_id = ? AND warehouse_id = ? AND quantity_in_transit >= ?",
            qty, qty, productId, warehouseId, qty);
    }
}
