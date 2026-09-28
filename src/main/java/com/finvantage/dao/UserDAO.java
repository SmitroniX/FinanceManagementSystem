package com.finvantage.dao;

import com.finvantage.model.User;
import com.finvantage.service.DatabaseConnectionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for USERS entity in Oracle Database.
 * Implements prepared statements to guarantee SQL injection safety.
 */
public class UserDAO implements GenericDAO<User, Long> {

    private final DatabaseConnectionService dbService = DatabaseConnectionService.getInstance();

    @Override
    public User save(User user) throws SQLException {
        String sql = "INSERT INTO USERS (FULL_NAME, EMAIL, PASSWORD_HASH, ROLE, PHONE_NUMBER, STATUS) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"USER_ID"})) {
            
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getPhoneNumber());
            ps.setString(6, user.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getLong(1));
                    }
                }
            }
            return user;
        }
    }

    @Override
    public Optional<User> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE USER_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE LOWER(EMAIL) = LOWER(?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email != null ? email.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM USERS ORDER BY USER_ID ASC";
        try (Connection conn = dbService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    @Override
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE USERS SET FULL_NAME = ?, ROLE = ?, PHONE_NUMBER = ?, STATUS = ?, UPDATED_AT = CURRENT_TIMESTAMP WHERE USER_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getRole().name());
            ps.setString(3, user.getPhoneNumber());
            ps.setString(4, user.getStatus());
            ps.setLong(5, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM USERS WHERE USER_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getLong("USER_ID"),
            rs.getString("FULL_NAME"),
            rs.getString("EMAIL"),
            rs.getString("PASSWORD_HASH"),
            User.Role.valueOf(rs.getString("ROLE"))
        );
        user.setPhoneNumber(rs.getString("PHONE_NUMBER"));
        user.setStatus(rs.getString("STATUS"));
        Timestamp created = rs.getTimestamp("CREATED_AT");
        if (created != null) user.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("UPDATED_AT");
        if (updated != null) user.setUpdatedAt(updated.toLocalDateTime());
        return user;
    }
}
