package com.inventory.alert.repository;

import com.inventory.alert.model.LowStockAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LowStockAlertRepository {
    private final JdbcTemplate jdbc;

    private static final RowMapper<LowStockAlert> M = (rs, n) -> {
        LowStockAlert a = new LowStockAlert();
        a.setId(rs.getLong("id"));
        a.setProductId(rs.getLong("product_id"));
        a.setProductName(rs.getString("product_name"));
        a.setSku(rs.getString("sku"));
        a.setWarehouseId(rs.getLong("warehouse_id"));
        a.setWarehouseCode(rs.getString("warehouse_code"));
        a.setCurrentQty(rs.getInt("current_qty"));
        a.setThreshold(rs.getInt("threshold"));
        a.setStatus(rs.getString("status"));
        Timestamp c = rs.getTimestamp("created_at"); if (c != null) a.setCreatedAt(c.toLocalDateTime());
        Timestamp r = rs.getTimestamp("resolved_at"); if (r != null) a.setResolvedAt(r.toLocalDateTime());
        return a;
    };

    private static final String BASE = """
        SELECT a.*, p.name AS product_name, p.sku AS sku, w.code AS warehouse_code
        FROM low_stock_alerts a JOIN products p ON a.product_id = p.id JOIN warehouses w ON a.warehouse_id = w.id
        """;

    public List<LowStockAlert> findAll(String status) {
        if (status == null || status.isBlank())
            return jdbc.query(BASE + " ORDER BY a.created_at DESC", M);
        return jdbc.query(BASE + " WHERE a.status = ? ORDER BY a.created_at DESC", M, status);
    }

    public Optional<LowStockAlert> findById(Long id) {
        var l = jdbc.query(BASE + " WHERE a.id = ?", M, id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    public boolean existsOpen(Long productId, Long warehouseId) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM low_stock_alerts WHERE product_id=? AND warehouse_id=? AND status='OPEN'", Integer.class, productId, warehouseId);
        return c != null && c > 0;
    }

    public LowStockAlert save(LowStockAlert a) {
        if (a.getId() == null) {
            jdbc.update("INSERT INTO low_stock_alerts (product_id,warehouse_id,current_qty,threshold,status) VALUES (?,?,?,?,?)",
                a.getProductId(), a.getWarehouseId(), a.getCurrentQty(), a.getThreshold(), a.getStatus() == null ? "OPEN" : a.getStatus());
        } else {
            jdbc.update("UPDATE low_stock_alerts SET status=?, resolved_at=? WHERE id=?", a.getStatus(), a.getResolvedAt(), a.getId());
        }
        return a;
    }

    public void resolve(Long id) {
        jdbc.update("UPDATE low_stock_alerts SET status='RESOLVED', resolved_at=CURRENT_TIMESTAMP WHERE id=?", id);
    }
}
