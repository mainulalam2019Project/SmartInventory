package com.inventory.warehouse.repository;

import com.inventory.warehouse.model.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WarehouseRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Warehouse> M = (rs, n) -> {
        Warehouse w = new Warehouse();
        w.setId(rs.getLong("id"));
        w.setCode(rs.getString("code"));
        w.setName(rs.getString("name"));
        w.setAddress(rs.getString("address"));
        w.setCity(rs.getString("city"));
        w.setCountry(rs.getString("country"));
        w.setCapacity(rs.getInt("capacity"));
        w.setActive(rs.getInt("active") == 1);
        return w;
    };
    public List<Warehouse> findAll() { return jdbc.query("SELECT * FROM warehouses ORDER BY id", M); }
    public Optional<Warehouse> findById(Long id) {
        var l = jdbc.query("SELECT * FROM warehouses WHERE id = ?", M, id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }
    public boolean existsByCode(String code) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM warehouses WHERE code = ?", Integer.class, code);
        return c != null && c > 0;
    }
    public Warehouse save(Warehouse w) {
        if (w.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement("INSERT INTO warehouses (code,name,address,city,country,capacity,active) VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, w.getCode()); ps.setString(2, w.getName()); ps.setString(3, w.getAddress());
                ps.setString(4, w.getCity()); ps.setString(5, w.getCountry()); ps.setInt(6, w.getCapacity()); ps.setInt(7, w.isActive()?1:0);
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) w.setId(k.longValue());
        } else {
            jdbc.update("UPDATE warehouses SET code=?,name=?,address=?,city=?,country=?,capacity=?,active=? WHERE id=?",
                w.getCode(), w.getName(), w.getAddress(), w.getCity(), w.getCountry(), w.getCapacity(), w.isActive()?1:0, w.getId());
        }
        return w;
    }
    public void delete(Long id) { jdbc.update("DELETE FROM warehouses WHERE id = ?", id); }
}
