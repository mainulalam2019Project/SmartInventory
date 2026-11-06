package com.inventory.supplier.repository;

import com.inventory.supplier.model.Supplier;
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
public class SupplierRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Supplier> M = (rs, n) -> {
        Supplier s = new Supplier();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        s.setCity(rs.getString("city"));
        s.setCountry(rs.getString("country"));
        s.setLeadTimeDays(rs.getInt("lead_time_days"));
        s.setRating(rs.getBigDecimal("rating"));
        s.setActive(rs.getInt("active") == 1);
        return s;
    };
    public List<Supplier> findAll() { return jdbc.query("SELECT * FROM suppliers ORDER BY id", M); }
    public Optional<Supplier> findById(Long id) {
        var l = jdbc.query("SELECT * FROM suppliers WHERE id = ?", M, id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }
    public Supplier save(Supplier s) {
        if (s.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement("INSERT INTO suppliers (name,contact_person,email,phone,address,city,country,lead_time_days,rating,active) VALUES (?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, s.getName()); ps.setString(2, s.getContactPerson()); ps.setString(3, s.getEmail());
                ps.setString(4, s.getPhone()); ps.setString(5, s.getAddress()); ps.setString(6, s.getCity());
                ps.setString(7, s.getCountry()); ps.setInt(8, s.getLeadTimeDays()); ps.setBigDecimal(9, s.getRating()); ps.setInt(10, s.isActive()?1:0);
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) s.setId(k.longValue());
        } else {
            jdbc.update("UPDATE suppliers SET name=?,contact_person=?,email=?,phone=?,address=?,city=?,country=?,lead_time_days=?,rating=?,active=? WHERE id=?",
                s.getName(), s.getContactPerson(), s.getEmail(), s.getPhone(), s.getAddress(), s.getCity(), s.getCountry(), s.getLeadTimeDays(), s.getRating(), s.isActive()?1:0, s.getId());
        }
        return s;
    }
    public void delete(Long id) { jdbc.update("DELETE FROM suppliers WHERE id = ?", id); }
}
