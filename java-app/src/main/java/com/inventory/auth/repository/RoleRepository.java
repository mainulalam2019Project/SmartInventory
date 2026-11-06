package com.inventory.auth.repository;

import com.inventory.auth.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Role> MAPPER = (rs, n) -> {
        Role r = new Role();
        r.setId(rs.getLong("id"));
        r.setName(rs.getString("name"));
        r.setDescription(rs.getString("description"));
        return r;
    };

    public List<Role> findAll() { return jdbc.query("SELECT * FROM roles ORDER BY id", MAPPER); }
    public Optional<Role> findById(Long id) {
        var l = jdbc.query("SELECT * FROM roles WHERE id = ?", MAPPER, id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }
    public Optional<Role> findByName(String name) {
        var l = jdbc.query("SELECT * FROM roles WHERE name = ?", MAPPER, name);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }
}
