package com.inventory.customer.repository;

import com.inventory.customer.model.Customer;
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
public class CustomerRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Customer> M = (rs, n) -> {
        Customer c = new Customer();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setCountry(rs.getString("country"));
        c.setType(rs.getString("type"));
        c.setActive(rs.getInt("active") == 1);
        return c;
    };
    public List<Customer> findAll() { return jdbc.query("SELECT * FROM customers ORDER BY id", M); }
    public Optional<Customer> findById(Long id) {
        var l = jdbc.query("SELECT * FROM customers WHERE id = ?", M, id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }
    public Customer save(Customer c) {
        if (c.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement("INSERT INTO customers (name,email,phone,address,city,country,type,active) VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, c.getName()); ps.setString(2, c.getEmail()); ps.setString(3, c.getPhone());
                ps.setString(4, c.getAddress()); ps.setString(5, c.getCity()); ps.setString(6, c.getCountry());
                ps.setString(7, c.getType() == null ? "RETAIL" : c.getType()); ps.setInt(8, c.isActive()?1:0);
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) c.setId(k.longValue());
        } else {
            jdbc.update("UPDATE customers SET name=?,email=?,phone=?,address=?,city=?,country=?,type=?,active=? WHERE id=?",
                c.getName(), c.getEmail(), c.getPhone(), c.getAddress(), c.getCity(), c.getCountry(), c.getType(), c.isActive()?1:0, c.getId());
        }
        return c;
    }
    public void delete(Long id) { jdbc.update("DELETE FROM customers WHERE id = ?", id); }
}
