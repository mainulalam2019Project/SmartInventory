package com.inventory.transfer.repository;

import com.inventory.transfer.model.StockTransfer;
import com.inventory.transfer.model.StockTransferItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockTransferRepository {
    private final JdbcTemplate jdbc;

    private static final RowMapper<StockTransfer> M = (rs, n) -> {
        StockTransfer t = new StockTransfer();
        t.setId(rs.getLong("id"));
        t.setTransferNumber(rs.getString("transfer_number"));
        t.setFromWarehouseId(rs.getLong("from_warehouse_id"));
        t.setFromWarehouseCode(rs.getString("from_code"));
        t.setToWarehouseId(rs.getLong("to_warehouse_id"));
        t.setToWarehouseCode(rs.getString("to_code"));
        t.setStatus(rs.getString("status"));
        long rb = rs.getLong("requested_by"); if (!rs.wasNull()) t.setRequestedBy(rb);
        long ab = rs.getLong("approved_by"); if (!rs.wasNull()) t.setApprovedBy(ab);
        String sh = rs.getString("shipped_at"); if (sh != null) t.setShippedAt(java.time.LocalDateTime.parse(sh.trim().replace(' ', 'T')));
        String rc = rs.getString("received_at"); if (rc != null) t.setReceivedAt(java.time.LocalDateTime.parse(rc.trim().replace(' ', 'T')));
        t.setNotes(rs.getString("notes"));
        String c = rs.getString("created_at"); if (c != null) t.setCreatedAt(java.time.LocalDateTime.parse(c.trim().replace(' ', 'T')));
        String u = rs.getString("updated_at"); if (u != null) t.setUpdatedAt(java.time.LocalDateTime.parse(u.trim().replace(' ', 'T')));
        return t;
    };

    private static final RowMapper<StockTransferItem> ITEM_M = (rs, n) -> {
        StockTransferItem it = new StockTransferItem();
        it.setId(rs.getLong("id"));
        it.setTransferId(rs.getLong("transfer_id"));
        it.setProductId(rs.getLong("product_id"));
        it.setProductName(rs.getString("product_name"));
        it.setSku(rs.getString("sku"));
        it.setQuantityRequested(rs.getInt("quantity_requested"));
        it.setQuantityShipped(rs.getInt("quantity_shipped"));
        it.setQuantityReceived(rs.getInt("quantity_received"));
        return it;
    };

    private static final String BASE = """
        SELECT t.*, fw.code AS from_code, tw.code AS to_code
        FROM stock_transfers t JOIN warehouses fw ON t.from_warehouse_id = fw.id JOIN warehouses tw ON t.to_warehouse_id = tw.id
        """;

    public List<StockTransfer> findAll() {
        List<StockTransfer> list = jdbc.query(BASE + " ORDER BY t.id DESC", M);
        for (StockTransfer t : list) t.setItems(findItems(t.getId()));
        return list;
    }

    public StockTransfer findById(Long id) {
        List<StockTransfer> list = jdbc.query(BASE + " WHERE t.id = ?", M, id);
        if (list.isEmpty()) return null;
        StockTransfer t = list.get(0);
        t.setItems(findItems(id));
        return t;
    }

    private List<StockTransferItem> findItems(Long id) {
        return jdbc.query("""
            SELECT ti.*, p.name AS product_name, p.sku AS sku
            FROM stock_transfer_items ti JOIN products p ON ti.product_id = p.id WHERE ti.transfer_id = ?""", ITEM_M, id);
    }

    public StockTransfer save(StockTransfer t) {
        if (t.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO stock_transfers (transfer_number,from_warehouse_id,to_warehouse_id,status,requested_by,notes) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, t.getTransferNumber()); ps.setLong(2, t.getFromWarehouseId()); ps.setLong(3, t.getToWarehouseId());
                ps.setString(4, t.getStatus() == null ? "REQUESTED" : t.getStatus());
                if (t.getRequestedBy() == null) ps.setNull(5, java.sql.Types.BIGINT); else ps.setLong(5, t.getRequestedBy());
                ps.setString(6, t.getNotes());
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) t.setId(k.longValue());
            for (StockTransferItem it : t.getItems()) {
                jdbc.update("INSERT INTO stock_transfer_items (transfer_id,product_id,quantity_requested,quantity_shipped,quantity_received) VALUES (?,?,?,?,0)",
                    t.getId(), it.getProductId(), it.getQuantityRequested(), 0);
            }
        } else {
            jdbc.update("UPDATE stock_transfers SET status=?, approved_by=?, shipped_at=?, received_at=?, notes=?, updated_at=CURRENT_TIMESTAMP WHERE id=?",
                t.getStatus(), t.getApprovedBy(), t.getShippedAt(), t.getReceivedAt(), t.getNotes(), t.getId());
        }
        return t;
    }

    public void updateStatus(Long id, String status, Long approvedBy, java.time.LocalDateTime shippedAt, java.time.LocalDateTime receivedAt) {
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        jdbc.update("UPDATE stock_transfers SET status=?, approved_by=COALESCE(?,approved_by), shipped_at=COALESCE(?,shipped_at), received_at=COALESCE(?,received_at), updated_at=CURRENT_TIMESTAMP WHERE id=?",
            status, approvedBy,
            shippedAt == null ? null : shippedAt.format(f),
            receivedAt == null ? null : receivedAt.format(f),
            id);
    }

    public void updateItemShipped(Long itemId, int qty) {
        jdbc.update("UPDATE stock_transfer_items SET quantity_shipped = quantity_shipped + ? WHERE id = ?", qty, itemId);
    }

    public void updateItemReceived(Long itemId, int qty) {
        jdbc.update("UPDATE stock_transfer_items SET quantity_received = quantity_received + ? WHERE id = ?", qty, itemId);
    }
}
