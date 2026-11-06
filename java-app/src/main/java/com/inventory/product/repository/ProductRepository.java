package com.inventory.product.repository;

import com.inventory.product.model.Product;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Product> M = (rs, n) -> {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSku(rs.getString("sku"));
        p.setName(rs.getString("name"));
        long c = rs.getLong("category_id"); if (rs.wasNull()) p.setCategoryId(null); else p.setCategoryId(c);
        p.setCategoryName(rs.getString("category_name"));
        p.setUnit(rs.getString("unit"));
        p.setBarcode(rs.getString("barcode"));
        p.setCostPrice(rs.getBigDecimal("cost_price"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setStatus(rs.getString("status"));
        p.setReorderPoint(rs.getInt("reorder_point"));
        p.setSafetyStock(rs.getInt("safety_stock"));
        Timestamp cr = rs.getTimestamp("created_at"); if (cr != null) p.setCreatedAt(cr.toLocalDateTime());
        Timestamp up = rs.getTimestamp("updated_at"); if (up != null) p.setUpdatedAt(up.toLocalDateTime());
        return p;
    };

    private static final String BASE = "SELECT p.*, c.name AS category_name FROM products p LEFT JOIN categories c ON p.category_id = c.id";

    public List<Product> findAll(String search, Long categoryId, String status) {
        StringBuilder sql = new StringBuilder(BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (p.sku LIKE ? OR p.name LIKE ?)");
            params.add("%" + search + "%"); params.add("%" + search + "%");
        }
        if (categoryId != null) { sql.append(" AND p.category_id = ?"); params.add(categoryId); }
        if (status != null && !status.isBlank()) { sql.append(" AND p.status = ?"); params.add(status); }
        sql.append(" ORDER BY p.id");
        return jdbc.query(sql.toString(), M, params.toArray());
    }

    public Optional<Product> findById(Long id) {
        var l = jdbc.query(BASE + " WHERE p.id = ?", M, id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    public Optional<Product> findBySku(String sku) {
        var l = jdbc.query(BASE + " WHERE p.sku = ?", M, sku);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    public boolean existsBySku(String sku) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM products WHERE sku = ?", Integer.class, sku);
        return c != null && c > 0;
    }

    public Product save(Product p) {
        if (p.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO products (sku,name,category_id,unit,barcode,cost_price,selling_price,status,reorder_point,safety_stock) VALUES (?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, p.getSku());
                ps.setString(2, p.getName());
                if (p.getCategoryId() == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setLong(3, p.getCategoryId());
                ps.setString(4, p.getUnit());
                ps.setString(5, p.getBarcode());
                ps.setBigDecimal(6, p.getCostPrice() == null ? BigDecimal.ZERO : p.getCostPrice());
                ps.setBigDecimal(7, p.getSellingPrice() == null ? BigDecimal.ZERO : p.getSellingPrice());
                ps.setString(8, p.getStatus() == null ? "ACTIVE" : p.getStatus());
                ps.setInt(9, p.getReorderPoint());
                ps.setInt(10, p.getSafetyStock());
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) p.setId(k.longValue());
        } else {
            jdbc.update("UPDATE products SET sku=?,name=?,category_id=?,unit=?,barcode=?,cost_price=?,selling_price=?,status=?,reorder_point=?,safety_stock=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",
                p.getSku(), p.getName(), p.getCategoryId(), p.getUnit(), p.getBarcode(), p.getCostPrice(), p.getSellingPrice(), p.getStatus(), p.getReorderPoint(), p.getSafetyStock(), p.getId());
        }
        return p;
    }

    public void delete(Long id) { jdbc.update("DELETE FROM products WHERE id = ?", id); }
}
