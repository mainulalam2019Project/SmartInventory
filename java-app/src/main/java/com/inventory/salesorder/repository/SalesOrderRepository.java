package com.inventory.salesorder.repository;

import com.inventory.salesorder.model.SalesOrder;
import com.inventory.salesorder.model.SalesOrderItem;
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
public class SalesOrderRepository {
    private final JdbcTemplate jdbc;

    private static final RowMapper<SalesOrder> M = (rs, n) -> {
        SalesOrder o = new SalesOrder();
        o.setId(rs.getLong("id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setCustomerId(rs.getLong("customer_id"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setWarehouseId(rs.getLong("warehouse_id"));
        o.setWarehouseCode(rs.getString("warehouse_code"));
        o.setStatus(rs.getString("status"));
        String od = rs.getString("order_date"); if (od != null) o.setOrderDate(java.time.LocalDate.parse(od.substring(0, 10)));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        long cb = rs.getLong("created_by"); if (!rs.wasNull()) o.setCreatedBy(cb);
        o.setNotes(rs.getString("notes"));
        Timestamp c = rs.getTimestamp("created_at"); if (c != null) o.setCreatedAt(c.toLocalDateTime());
        Timestamp u = rs.getTimestamp("updated_at"); if (u != null) o.setUpdatedAt(u.toLocalDateTime());
        return o;
    };

    private static final RowMapper<SalesOrderItem> ITEM_M = (rs, n) -> {
        SalesOrderItem it = new SalesOrderItem();
        it.setId(rs.getLong("id"));
        it.setOrderId(rs.getLong("order_id"));
        it.setProductId(rs.getLong("product_id"));
        it.setProductName(rs.getString("product_name"));
        it.setSku(rs.getString("sku"));
        it.setQuantity(rs.getInt("quantity"));
        it.setUnitPrice(rs.getBigDecimal("unit_price"));
        it.setQuantityFulfilled(rs.getInt("quantity_fulfilled"));
        return it;
    };

    private static final String BASE = """
        SELECT so.*, c.name AS customer_name, w.code AS warehouse_code
        FROM sales_orders so JOIN customers c ON so.customer_id = c.id JOIN warehouses w ON so.warehouse_id = w.id
        """;

    public List<SalesOrder> findAll() {
        List<SalesOrder> list = jdbc.query(BASE + " ORDER BY so.id DESC", M);
        for (SalesOrder o : list) o.setItems(findItems(o.getId()));
        return list;
    }

    public SalesOrder findById(Long id) {
        List<SalesOrder> list = jdbc.query(BASE + " WHERE so.id = ?", M, id);
        if (list.isEmpty()) return null;
        SalesOrder o = list.get(0);
        o.setItems(findItems(id));
        return o;
    }

    private List<SalesOrderItem> findItems(Long orderId) {
        return jdbc.query("""
            SELECT soi.*, p.name AS product_name, p.sku AS sku
            FROM sales_order_items soi JOIN products p ON soi.product_id = p.id WHERE soi.order_id = ?""", ITEM_M, orderId);
    }

    public SalesOrder save(SalesOrder o) {
        if (o.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO sales_orders (order_number,customer_id,warehouse_id,status,order_date,total_amount,created_by,notes) VALUES (?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, o.getOrderNumber()); ps.setLong(2, o.getCustomerId()); ps.setLong(3, o.getWarehouseId());
                ps.setString(4, o.getStatus() == null ? "PENDING" : o.getStatus());
                ps.setString(5, (o.getOrderDate() == null ? LocalDate.now() : o.getOrderDate()).toString());
                ps.setBigDecimal(6, o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount());
                if (o.getCreatedBy() == null) ps.setNull(7, java.sql.Types.BIGINT); else ps.setLong(7, o.getCreatedBy());
                ps.setString(8, o.getNotes());
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) o.setId(k.longValue());
            for (SalesOrderItem it : o.getItems()) {
                jdbc.update("INSERT INTO sales_order_items (order_id,product_id,quantity,unit_price,quantity_fulfilled) VALUES (?,?,?,?,0)",
                    o.getId(), it.getProductId(), it.getQuantity(), it.getUnitPrice());
            }
        } else {
            jdbc.update("UPDATE sales_orders SET status=?, notes=?, updated_at=CURRENT_TIMESTAMP WHERE id=?", o.getStatus(), o.getNotes(), o.getId());
        }
        return o;
    }

    public void updateStatus(Long id, String status) {
        jdbc.update("UPDATE sales_orders SET status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?", status, id);
    }

    public void updateItemFulfilled(Long itemId, int qty) {
        jdbc.update("UPDATE sales_order_items SET quantity_fulfilled = quantity_fulfilled + ? WHERE id = ?", qty, itemId);
    }

    public SalesOrderItem findItem(Long itemId) {
        List<SalesOrderItem> l = jdbc.query("""
            SELECT soi.*, p.name AS product_name, p.sku AS sku
            FROM sales_order_items soi JOIN products p ON soi.product_id = p.id WHERE soi.id = ?""", ITEM_M, itemId);
        return l.isEmpty() ? null : l.get(0);
    }
}
