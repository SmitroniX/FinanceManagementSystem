package com.finvantage.dao;

import com.finvantage.model.Category;
import com.finvantage.service.DatabaseConnectionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for CATEGORIES entity in Oracle Database.
 */
public class CategoryDAO implements GenericDAO<Category, Long> {

    private final DatabaseConnectionService dbService = DatabaseConnectionService.getInstance();

    @Override
    public Category save(Category category) throws SQLException {
        String sql = "INSERT INTO CATEGORIES (USER_ID, NAME, CATEGORY_TYPE, COLOR_HEX, ICON_NAME) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"CATEGORY_ID"})) {
            ps.setLong(1, category.getUserId());
            ps.setString(2, category.getName());
            ps.setString(3, category.getType().name());
            ps.setString(4, category.getColorHex());
            ps.setString(5, category.getIconName());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setId(rs.getLong(1));
                    }
                }
            }
            return category;
        }
    }

    @Override
    public Optional<Category> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM CATEGORIES WHERE CATEGORY_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Category> findByUserId(Long userId) throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM CATEGORIES WHERE USER_ID = ? OR USER_ID IS NULL ORDER BY CATEGORY_TYPE, NAME ASC";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCategory(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM CATEGORIES ORDER BY NAME ASC";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToCategory(rs));
            }
        }
        return list;
    }

    @Override
    public boolean update(Category entity) throws SQLException {
        String sql = "UPDATE CATEGORIES SET NAME = ?, COLOR_HEX = ?, ICON_NAME = ? WHERE CATEGORY_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getColorHex());
            ps.setString(3, entity.getIconName());
            ps.setLong(4, entity.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM CATEGORIES WHERE CATEGORY_ID = ?";
        try (Connection conn = dbService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Long id = rs.getLong("CATEGORY_ID");
        Long userId = rs.getLong("USER_ID");
        if (rs.wasNull()) userId = null;
        String name = rs.getString("NAME");
        String type = rs.getString("CATEGORY_TYPE");
        String color = rs.getString("COLOR_HEX");
        String icon = rs.getString("ICON_NAME");

        Category cat = new Category(id, userId, name, Category.Type.valueOf(type), color, icon);
        Timestamp created = rs.getTimestamp("CREATED_AT");
        if (created != null) cat.setCreatedAt(created.toLocalDateTime());
        return cat;
    }
}
