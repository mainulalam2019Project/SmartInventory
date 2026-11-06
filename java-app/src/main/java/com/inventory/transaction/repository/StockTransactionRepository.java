package com.inventory.transaction.repository;

import com.inventory.transaction.model.StockTransaction;
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

@Repository
@RequiredArgsConstructor
public class StockTransactionRepository {
    private final JdbcTemplate jdbc;

    private static final RowMapper<StockTransaction> M = (rs, n) -> {
        StockTransaction t = new StockTransaction();
        t.setId(rs.getLong("id"));
        t.setProductId(rs.getLong("product_id"));
        t.setProductName(rs.getString("product_name"));
        t.setSku(rs.getString("sku"));
        t.setWarehouseId(rs.getLong("warehouse_id"));
        t.setWarehouseCode(rs.getString("warehouse_code"));
        t.setType(rs.getString("type"));
        t.setQuantity(rs.getInt("quantity"));
        t.setReferenceType(rs.getString("reference_type"));
        long r = rs.getLong("reference_id"); if (rs.wasNull()) t.setReferenceId(null); else t.setReferenceId(r);
        long p = rs.getLong("performed_by"); if (rs.wasNull()) t.setPerformedBy(null); else t.setPerformedBy(p);
        Timestamp o = rs.getTimestamp("occurred_at"); if (o != null) t.setOccurredAt(o.toLocalDateTime());
        t.setNotes(rs.getString("notes"));
        return t;
    };

    private static final String BASE = """
        SELECT t.*, p.name AS product_name, p.sku AS sku, w.code AS warehouse_code
        FROM stock_transactions t JOIN products p ON t.product_id = p.id JOIN warehouses w ON t.warehouse_id = w.id
        """;

    public List<StockTransaction> findAll(Long productId, Long warehouseId, String type, int limit) {
        StringBuilder sql = new StringBuilder(BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (productId != null) { sql.append(" AND t.product_id = ?"); params.add(productId); }
        if (warehouseId != null) { sql.append(" AND t.warehouse_id = ?"); params.add(warehouseId); }
        if (type != null && !type.isBlank()) { sql.append(" AND t.type = ?"); params.add(type); }
        sql.append(" ORDER BY t.occurred_at DESC, t.id DESC LIMIT ?");
        params.add(limit > 0 ? limit : 100);
        return jdbc.query(sql.toString(), M, params.toArray());
    }

    public StockTransaction save(StockTransaction t) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO stock_transactions (product_id,warehouse_id,type,quantity,reference_type,reference_id,performed_by,notes) VALUES (?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, t.getProductId()); ps.setLong(2, t.getWarehouseId()); ps.setString(3, t.getType());
            ps.setInt(4, t.getQuantity());
            if (t.getReferenceType() == null) ps.setNull(5, java.sql.Types.VARCHAR); else ps.setString(5, t.getReferenceType());
            if (t.getReferenceId() == null) ps.setNull(6, java.sql.Types.BIGINT); else ps.setLong(6, t.getReferenceId());
            if (t.getPerformedBy() == null) ps.setNull(7, java.sql.Types.BIGINT); else ps.setLong(7, t.getPerformedBy());
            ps.setString(8, t.getNotes());
            return ps;
        }, kh);
        Number k = kh.getKey(); if (k != null) t.setId(k.longValue());
        return t;
    }
}
