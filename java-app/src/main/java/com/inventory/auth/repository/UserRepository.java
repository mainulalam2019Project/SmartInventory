package com.inventory.auth.repository;

import com.inventory.auth.model.User;
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
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<User> MAPPER = (rs, n) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRoleId(rs.getLong("role_id"));
        u.setRoleName(rs.getString("role_name"));
        u.setActive(rs.getInt("active") == 1);
        Timestamp c = rs.getTimestamp("created_at");
        if (c != null) u.setCreatedAt(c.toLocalDateTime());
        Timestamp up = rs.getTimestamp("updated_at");
        if (up != null) u.setUpdatedAt(up.toLocalDateTime());
        return u;
    };

    public Optional<User> findByUsername(String username) {
        List<User> list = jdbc.query(
            "SELECT u.*, r.name AS role_name FROM users u JOIN roles r ON u.role_id = r.id WHERE u.username = ?",
            MAPPER, username);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<User> findById(Long id) {
        List<User> list = jdbc.query(
            "SELECT u.*, r.name AS role_name FROM users u JOIN roles r ON u.role_id = r.id WHERE u.id = ?",
            MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<User> findAll() {
        return jdbc.query(
            "SELECT u.*, r.name AS role_name FROM users u JOIN roles r ON u.role_id = r.id ORDER BY u.id", MAPPER);
    }

    public boolean existsByUsername(String username) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        return c != null && c > 0;
    }

    public boolean existsByEmail(String email) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return c != null && c > 0;
    }

    public User save(User u) {
        if (u.getId() == null) {
            KeyHolder kh = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users (username, email, password_hash, role_id, active) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, u.getUsername());
                ps.setString(2, u.getEmail());
                ps.setString(3, u.getPasswordHash());
                ps.setLong(4, u.getRoleId());
                ps.setInt(5, u.isActive() ? 1 : 0);
                return ps;
            }, kh);
            Number key = kh.getKey();
            if (key != null) u.setId(key.longValue());
        } else {
            jdbc.update("UPDATE users SET username=?, email=?, role_id=?, active=?, updated_at=CURRENT_TIMESTAMP WHERE id=?",
                u.getUsername(), u.getEmail(), u.getRoleId(), u.isActive() ? 1 : 0, u.getId());
        }
        return u;
    }

    public void updatePassword(Long id, String hash) {
        jdbc.update("UPDATE users SET password_hash=?, updated_at=CURRENT_TIMESTAMP WHERE id=?", hash, id);
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }
}
