package com.inventory.category.repository;

import com.inventory.category.model.Category;
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
public class CategoryRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Category> M = (rs, n) -> {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        long p = rs.getLong("parent_id"); if (rs.wasNull()) c.setParentId(null); else c.setParentId(p);
        c.setDescription(rs.getString("description"));
        return c;
    };
    public List<Category> findAll() { return jdbc.query("SELECT * FROM categories ORDER BY id", M); }
    public Optional<Category> findById(Long id) {
        var l = jdbc.query("SELECT * FROM categories WHERE id = ?", M, id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }
    public Category save(Category c) {
        if (c.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement("INSERT INTO categories (name, parent_id, description) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, c.getName());
                if (c.getParentId() == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setLong(2, c.getParentId());
                ps.setString(3, c.getDescription());
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) c.setId(k.longValue());
        } else {
            jdbc.update("UPDATE categories SET name=?, parent_id=?, description=? WHERE id=?",
                c.getName(), c.getParentId(), c.getDescription(), c.getId());
        }
        return c;
    }
    public void delete(Long id) { jdbc.update("DELETE FROM categories WHERE id = ?", id); }
}
